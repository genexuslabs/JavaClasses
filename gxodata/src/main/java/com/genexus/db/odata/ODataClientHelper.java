package com.genexus.db.odata;

import com.genexus.GXutil;
import com.genexus.db.service.ServiceError;
import com.genexus.db.service.ServiceException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import org.apache.olingo.client.api.domain.ClientCollectionValue;
import org.apache.olingo.client.api.domain.ClientComplexValue;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientObjectFactory;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.domain.ClientValue;
import org.apache.olingo.client.api.uri.FilterArg;
import org.apache.olingo.client.api.uri.FilterArgFactory;
import org.apache.olingo.client.api.uri.FilterFactory;
import org.apache.olingo.client.api.uri.QueryOption;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.api.uri.URIFilter;
import org.apache.olingo.client.core.uri.FilterLiteral;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.core.Encoder;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;

public class ODataClientHelper
{
    protected ODataClientHelper(ODataConnection con, ClientEntity[] oEntity, EdmEntityType[] oEntityType)
    {
        this.con = con;
        this.oEntity = oEntity;
        this.oEntityType = oEntityType;
        this.builder = newURIBuilder();        
    }
    
    List<EdmEntityType> entities = new ArrayList<>();
    HashMap<String,EdmNavigationProperty> usedNavProps = new HashMap<>();
    HashMap<String, List<GxURIFilter>> filters = new HashMap<>();
    ClientEntity [] oEntity;
    EdmEntityType[] oEntityType;
    EdmEntityType baseType;
    String baseEntitySet;
    List<ExpandItem> expands = new ArrayList<>();
    List<String> constrainedProps = new ArrayList<>();
    boolean replaceFilterWithKey;
    boolean isCurrentOfUpdate;
    
    public ODataClientHelper get(String entity, String... select)
    {
        String entityName = entity;//con.entity(entity);
        baseEntitySet = entityName;
        builder = builder.appendEntitySetSegment(entityName);
        if(select.length != 0)
            builder = builder.select(select);
        EdmEntityType entityType = con.getModel().getEntityContainer().getEntitySet(entityName).getEntityType();
        entities.add(entityType);
        baseType = entityType; 
        if(oEntity != null)
        {
            replaceFilterWithKey = true; // Aviso que quiero armar una SegmentKey
            if(oEntity[0] == null)
                oEntity[0] = con.client.getObjectFactory().newEntity(entityType.getFullQualifiedName()); 
        }
        if(oEntityType != null)
            oEntityType[0] = entityType;
        return this;
    }    
    
    public ODataClientHelper expand(String entity, String... select)
    {
        String entityName = con.entity(baseType, entity);
        if(entityName == null)
        {
            for(EdmEntityType entityType:entities)
            {
                String mappedEntity = con.entity(entityType, entity);
                if(mappedEntity != null)
                    entityName = mappedEntity;
                EdmNavigationProperty navProp = entityType.getNavigationProperty(entityName);
                if(navProp != null)
                {
                    entities.add(navProp.getType());
                    usedNavProps.put(entityName, navProp);
                    break;
                }
            }
        }else
        {
            EdmNavigationProperty navProp = baseType.getNavigationProperty(entityName);
                if(navProp != null)
                {
                    entities.add(navProp.getType());
                    usedNavProps.put(entityName, navProp);
                }                
        }
        if(entityName == null)
            entityName = entity;
        expands.add(new ExpandItem(entity, entityName, select));
        return this;
    }
    
    public List<String> getConstrainedProps()
    {
        return constrainedProps;
    }
    
// Actualizaciones
    
    private EdmEntityType getEntityType(ClientEntity entity)
    {
        return con.getModel().getEntityType(entity.getTypeName());
    }
    
    public ODataClientHelper set(String key, Object value)
    {
        if(oEntity == null)
            return this;
        ClientEntity entity = getUpdateEntity();
        ClientProperty prop = entity.getProperty(key);
        if(prop == null || prop.hasPrimitiveValue() || prop.hasEnumValue())
        {
            List<ClientProperty> props = entity.getProperties();
            if(prop != null)
                props.remove(prop);
            ClientObjectFactory objFactory = getObjFactory();
            props.add(newProperty(objFactory, key, value, getEntityType(entity).getProperty(key).getType()));
        }else
        {
            if(prop.hasCollectionValue())
            {
                if(!prop.getCollectionValue().asJavaCollection().contains(value))
                {
                    ClientPrimitiveValue.Builder builder = getObjFactory().newPrimitiveValueBuilder();
                    builder.setValue(value);
                    builder.setType(getEntityType(entity).getProperty(key).getType());
                    prop.getCollectionValue().add(builder.build());
                }else throw new ServiceException(ServiceError.DUPLICATE_KEY.toString(), ServiceError.DUPLICATE_KEY.getSqlState(), ServiceError.DUPLICATE_KEY.getCode());
            }
        }        
        return this;
    }
    
    public ODataClientHelper set(ComplexHashMap complex)
    {
        ClientEntity entity;
        if(complex instanceof CurrentOf)
        {
            CurrentOf currentOf = (CurrentOf)complex;
            entity = currentOf.getUpdateEntity();
            oEntity[0] = entity;
            removeComplex(entity, currentOf.getCurrentOfEntry(), complex.getEntity(), true);
            isCurrentOfUpdate = true;
        }else 
        {
            if(oEntity == null)
                return this; // Caso que mando un "select" para poder hacer un executeUpdate
            entity = getUpdateEntity();
        }
        // Puede ser una propiedad estructurada o una navigation property
        String key = complex.getEntity();
        EdmEntityType entityType = getEntityType(entity);
        EdmElement complexTypeProp = entityType.getStructuralProperty(key);
        if(complexTypeProp != null)
            setComplexProperty(entity, complexTypeProp, (EdmComplexType)complexTypeProp.getType(), complex, key);
        else
        {
            EdmNavigationProperty navProp = entityType.getNavigationProperty(key);
            if(navProp != null)
            {
                setLink(entity, navProp.getType(), complex, key);
            }
        }
        return this;
    }
    
    public ODataClientHelper link(ComplexHashMap complex)
    {
        EdmEntityType entityType = getEntityType(getUpdateEntity());
        linkEntity = complex.getEntity();
        builder = builder.appendEntitySetSegment(linkEntity);
        if(!complex.values().contains(null))
        { // Si es link
            isLink = true;
            builder = builder.appendKeySegment(complex);
        }
        else isLink = false;  // Si hay algun nulo es unlink
        linkEntity = con.entity(entityType, complex.getEntity());
        return this;
    }    
    
    private void setLink(ClientEntity entity, EdmEntityType linkEntityType, ComplexHashMap complex, String key)
    {
        ClientObjectFactory objFactory = getObjFactory();
        
//        URI link = newURIBuilder().appendEntityIdSegment(linkEntityType.getName()).appendKeySegment(complex).build();
        URI link = newURIBuilder().appendEntitySetSegment(linkEntityType.getName()).appendKeySegment(complex).build();
        entity.addLink(objFactory.newEntityNavigationLink(key, link));        
    }
    
    private void setComplexProperty(ClientEntity entity, EdmElement complexTypeProp, EdmComplexType complexType, ComplexHashMap complex, String key)
    {
        ClientComplexValue value;
        boolean isCollection = complexTypeProp.isCollection();
        ClientObjectFactory objFactory = getObjFactory();
        ClientProperty prop = entity.getProperty(key);
        if(prop == null)
        {
            String complexTypeName = complexType.getFullQualifiedName().getFullQualifiedNameAsString();
            value = objFactory.newComplexValue(complexTypeName);
            if(isCollection)
            {
                ClientCollectionValue<ClientValue> colValue = objFactory.newCollectionValue(complexTypeName);
                objFactory.newCollectionProperty(complexTypeName, colValue);
                entity.getProperties().add(objFactory.newCollectionProperty(key, colValue));
                colValue.add(value);
            }else
            {            
                entity.getProperties().add(newProperty(objFactory, key, value, complexType));
            }
            fillClientValue(objFactory, complexType, value, complex);
        }else
        { 
            if(prop.hasCollectionValue())
            {// Puede ser una collection de estructurados
                value = objFactory.newComplexValue(complexType.getFullQualifiedName().getFullQualifiedNameAsString());
                fillClientValue(objFactory, complexType, value, complex);
                if(!prop.getCollectionValue().asJavaCollection().contains(value.asJavaMap()))
                    prop.getCollectionValue().add(value);
                else throw new ServiceException(ServiceError.DUPLICATE_KEY.toString(), ServiceError.DUPLICATE_KEY.getSqlState(), ServiceError.DUPLICATE_KEY.getCode());
            }else
            {
                fillClientValue(objFactory, complexType, prop.getComplexValue(), complex);
            }
        }        
    }
    
    public ODataClientHelper remove(ComplexHashMap complex)
    {
        ClientEntity entity = getUpdateEntity();
        removeComplex(entity, complex, complex.getEntity(), false);
        return this;
    }
    
    private boolean removeComplex(ClientEntity entity, HashMap<String, Object> complex, String key, boolean fullCheck)
    {
        ClientComplexValue value;
        EdmElement complexTypeProp = getEntityType(entity).getProperty(key);
        EdmComplexType complexType = (EdmComplexType)complexTypeProp.getType();
        ClientObjectFactory objFactory = getObjFactory();
        ClientProperty prop = entity.getProperty(key);
        boolean found = false;
        if(prop != null)
        { 
            if(prop.hasCollectionValue())
            {
                ClientCollectionValue<ClientValue> col = prop.getCollectionValue();
                if(!col.isEmpty())
                {
                    String complexTypeName = complexType.getFullQualifiedName().getFullQualifiedNameAsString();
                    value = objFactory.newComplexValue(complexTypeName);
                    fillClientValue(objFactory, complexType, value, complex);
                    Map<String, Object> valueMap = value.asJavaMap();
                    if(containsValue(col.asJavaCollection(), valueMap, fullCheck))
                    {
                        found = true;
                        ClientCollectionValue<ClientValue> colValue = objFactory.newCollectionValue(complexTypeName);
                        entity.getProperties().add(objFactory.newCollectionProperty(key, colValue));
                        for(ClientValue item:col)
                        {
                            if(!equalMap(item.asComplex().asJavaMap(), valueMap, fullCheck))
                            {
                                colValue.add(item);
                            }
                        }
                    }
                }
            }
            entity.getProperties().remove(prop);
        }
        return found;
    }

    private boolean containsValue(Collection<Object> col, Map<String, Object> valueMap, boolean fullCheck)
    {
        for(Object item:col)
        {
            if(item instanceof Map)
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemAsMap = (Map<String, Object>)item;
                if(equalMap(itemAsMap, valueMap, fullCheck))
                    return true;
            }
        }
        return false;
    }

    private boolean equalMap(Map<String, Object> itemMap, Map<String, Object> valueMap, boolean fullCheck)
    {
        if((fullCheck && itemMap.size() != valueMap.size()) ||
           (!fullCheck && itemMap.size() < valueMap.size()))
            return false;
        for(Map.Entry<String, Object> item:itemMap.entrySet())
        {
            String itemKey = item.getKey();
            Object itemValue = item.getValue();
            Object valueMapValue = valueMap.get(itemKey);
            if(itemValue != null && valueMapValue == null && fullCheck)
                return false;
            else if(itemValue != null && valueMapValue != null)
            {
                if(itemValue instanceof Map && valueMapValue instanceof Map)
                {
		                @SuppressWarnings("unchecked")
		                Map<String, Object> itemValueAsMap = (Map<String, Object>)itemValue;
		                @SuppressWarnings("unchecked")
		                Map<String, Object> valueMapValueAsMap = (Map<String, Object>)valueMapValue;
                    if(!equalMap(itemValueAsMap, valueMapValueAsMap, fullCheck))
                        return false;
                }
                else if(!itemValue.toString().equals(valueMapValue.toString()))
                    return false;
            }
        }
        return true;        
    }

    private void fillClientValue(ClientObjectFactory objFactory, EdmComplexType complexType, ClientComplexValue value, HashMap<String, Object> complex)
    {
        for(Map.Entry<String, Object> item:complex.entrySet())
        {
            String key = item.getKey();
            Object memberValue = item.getValue();
            if(memberValue instanceof HashMap)
            {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> childValue = (HashMap<String, Object>)memberValue;
                EdmComplexType childComplexType = (EdmComplexType)complexType.getProperty(key).getType();
                ClientProperty childProp = value.get(key);
                if(childProp == null)
                {
                    childProp = newProperty(objFactory, key, objFactory.newComplexValue(childComplexType.getFullQualifiedName().getFullQualifiedNameAsString()), childComplexType);
                    value.add(childProp);
                }
                fillClientValue(objFactory, childComplexType, childProp.getComplexValue(), childValue);
            }else
            {
                value.add(newProperty(objFactory, key, memberValue, complexType.getStructuralProperty(key).getType()));
            }
        }
    }    
    
    public ODataClientHelper remove(String key, Object value)
    {
        ClientEntity entity = getUpdateEntity();
        ClientProperty prop = entity.getProperty(key);
        List<ClientProperty> props = entity.getProperties();
        if(prop != null && prop.hasCollectionValue())
            removeClientCollectionValue(props, prop, key, value);
        else
        {
            ClientObjectFactory objFactory = getObjFactory();
            props.remove(newProperty(objFactory, key, value, getEntityType(entity).getProperty(key).getType()));
        }
        
        return this;
    }
    
    private boolean removeClientCollectionValue(List<ClientProperty> props, ClientProperty prop, String key, Object value)
    {
        ClientCollectionValue values = prop.getValue().asCollection();        
        ClientObjectFactory objFactory = getObjFactory();
        boolean found = false;
        ClientCollectionValue<? extends ClientValue> newValues = objFactory.newCollectionValue(prop.getValue().getTypeName());
        for(Object itemObj : values)
        {
            if(itemObj instanceof ClientValue)
            {
                ClientValue item = (ClientValue)itemObj;
                if(value.equals(item.asPrimitive().toValue()))
                {
                    found = true;
                }else
                {
                    newValues.add(item);                        
                }
            }
        }
        if(found)
        {
            props.remove(prop); 
            props.add(objFactory.newCollectionProperty(key, newValues));
        }
        return found;
    }
        
    Map<String, Object> keySegment;
    public ODataClientHelper key()
    {
        ClientEntity entity = getUpdateEntity();
        keySegment = new HashMap<>();
        
        for(EdmKeyPropertyRef keyRef : baseType.getKeyPropertyRefs())
        {
            keySegment.put(keyRef.getName(), getPropertyValue(entity.getProperty(keyRef.getName())));
        }
        entity.getNavigationLinks().clear();
        return this;
    }
    
    private boolean isLink, hasKeySegment;
    private String linkEntity = null;
    public ODataClientHelper keyRef(String linkEntity)
    {
        this.linkEntity = linkEntity;
        return key();
    }
    
    public String getLinkEntity()
    {
        return linkEntity;
    }
    
    public boolean isLink()
    {
        return isLink;
    }
    
    public boolean hasKeySegment()
    {
        return hasKeySegment;
    }    
    
    private ClientEntity getUpdateEntity()
    {
        return oEntity[0];
    }
    
    public boolean isCurrentOfUpdate()
    {
        return isCurrentOfUpdate;
    }    
    
    private boolean setsNullToNonStringType = false;
    public boolean getSetsNullToNonStringType()
    {
        //@hack: Olingo 4.5 no esta armando bien el Json de un PATCH cuando se manda un campo con null (la metadata full indica que el tipo de dato es String)
        //https://issues.apache.org/jira/browse/OLINGO-1114
        return setsNullToNonStringType;
    }    
    
    private Object getPropertyValue(ClientProperty prop)
    {
        ClientValue clientValue = prop.getValue();
        if(clientValue.isPrimitive())
            return clientValue.asPrimitive().toValue();
        else if(clientValue.isEnum())
            return clientValue.asEnum().getValue();
        else if(clientValue.isComplex())
            return clientValue.asComplex().asJavaMap();
        else if(clientValue.isCollection())
            return clientValue.asCollection().asJavaCollection();
        else return null;
    }
    
    
// Fin actualizaciones

// Consultas    
    public ODataClientHelper orderBy(String name)
    {
        return orderBy(null, name);
    }    
    
    public ODataClientHelper orderBy(String entity, String name)
    {
        String entityAndName = getEntityAndName(entity, name);
        if(entityAndName == null)
            return this;
        order = order == null ? entityAndName : String.format("%s, %s", order, entityAndName);
        return this;
    }    
    
    public ODataClientHelper orderByDesc(String name)
    {
        return orderByDesc(null, name);
    }    

    public ODataClientHelper orderByDesc(String entity, String name)
    {
        orderBy(entity, name);
        order = String.format("%s desc", order);
        return this;
    }    

    public ODataClientHelper thenBy(String name)
    {
        return thenBy(null, name);
    }    
    
    public ODataClientHelper thenBy(String entity, String name)
    {
        return orderBy(entity, name);
    }    

    public ODataClientHelper thenByDesc(String name)
    {
        return thenByDesc(null, name);
    }    
    
    public ODataClientHelper thenByDesc(String entity, String name)
    {
        return orderByDesc(entity, name);
    }
    
    public ODataClientHelper filter(GxURIFilter filter)
    {
        try
        {
            String currentFilterEntity;
            if(currentFilterEntities == null)
                currentFilterEntity = null;
            else if (currentFilterEntities.size() == 1)
                currentFilterEntity = currentFilterEntities.iterator().next();
            else
            {
                // El filtro va sobre mas de una entidad, veo si puedo partirla en más de una condición
                if(splitInvalidFilter(filter))
                    return this;
                if(currentFilterEntities.remove(null))
                    currentFilterEntities.add("<Base>");
                String [] entities = new String[currentFilterEntities.size()];            
                throw new UnsupportedOperationException(String.format("Condition cannot be evaluated: %s because it accesses members from two or more entities (%s)", filter.build(), String.join(", ", currentFilterEntities.toArray(entities))));
            }
            addFilter(currentFilterEntity, filter);
            return this;        
        }finally
        {
            currentFilterEntities = null;            
        }
    }
    
    private void addFilter(String currentFilterEntity, GxURIFilter filter)
    {
        List<GxURIFilter> currentFilters = filters.get(currentFilterEntity);
        if(currentFilters == null)
        {
            currentFilters = new ArrayList<>();
            filters.put(currentFilterEntity, currentFilters);
        }
        currentFilters.add(filter);        
    }
    
    private boolean splitInvalidFilter(GxURIFilter filter)
    {
        Stack<GxURIFilter> filtersToProcess = new Stack<>();
        Map<GxURIFilter, String> okFilters = new HashMap<>();
        filtersToProcess.add(filter);
        while(!filtersToProcess.empty())
        {
            filter = filtersToProcess.pop();
            Set<String> entitiesSet = new HashSet<>();
            if(filter.fillEntitiesSet(entitiesSet).size() > 1)
            {
                if(filter.op == GxURIOp.AND)
                {
                    GxURIFilterLogical andFilter = (GxURIFilterLogical)filter;
                    filtersToProcess.add(andFilter.right);
                    filtersToProcess.add(andFilter.left);
                    continue;
                }else if(filter.op == GxURIOp.NOT)
                {
                    boolean notParity = false;
                    GxURIFilter notFilter;
                    for(notFilter = ((GxURIFilterLogicalUnary)filter).left; notFilter.op == GxURIOp.NOT; notFilter = ((GxURIFilterLogicalUnary)notFilter).left)
                        notParity = !notParity;
                    if(notParity)
                    {
                        filtersToProcess.add(notFilter);
                        continue;
                    }
                    if(notFilter.op == GxURIOp.OR)
                    { // DeMorgan
                        GxURIFilterLogical orFilter = (GxURIFilterLogical)notFilter;
                        filtersToProcess.add(not(orFilter.right));
                        filtersToProcess.add(not(orFilter.left));
                        continue;
                    }
                }
                return false; // No pude partir el filtro
            }else
            {
                okFilters.put(filter, entitiesSet.isEmpty() ? null : entitiesSet.iterator().next());
            }
        }
        for(Map.Entry<GxURIFilter, String> item:okFilters.entrySet())
        {
            GxURIFilter okFilter = item.getKey();
            addFilter(item.getValue(), okFilter);
        }
        return true;
    }

    public ODataConnection con;
    private URIBuilder builder;
    private String order = null;
    public URI build(ODataQuery query, Object[] parms)
    {
        try
        {
            if(builder == null)
                builder = newURIBuilder();
            if(query != null)
                query.query.apply(this, parms);
            if(order != null)
                builder = builder.orderBy(order);
            for(ExpandItem expand : expands)
            {
                String entity = expand.entity;
                List<GxURIFilter> expandFilters = filters.get(entity);
                if(expandFilters == null)
                    builder = builder.expandWithSelect(expand.entityName, expand.select);
                else
                {
                    EnumMap<QueryOption, Object> options = new EnumMap<>(QueryOption.class);
                    options.put(QueryOption.SELECT, expand.getSelect());

                    GxURIFilter filter = null;
                    for(GxURIFilter other:expandFilters)
                    {
                        other = other.simplify();
                        if(other == GxURIFilterBooleanConst.TRUE)
                            continue;
                        filter = (filter == null ? other : and(filter, other));
                    }
                    if(filter != null)
                    {
                        options.put(QueryOption.FILTER, filter.build());
                        constrainedProps.add(expand.entityName);
                    }
//                    options.put(QueryOption.COUNT, true);
                    builder = builder.expandWithOptions(expand.entityName, options);                    
                }
            }
            List<GxURIFilter> baseFilters = filters.get(null);
            if(replaceFilterWithKey)
            {
                if(keySegment == null)
                    keySegment = setKeySegmentFromFilters(baseFilters);
                if(keySegment != null)
                {
                    builder = builder.appendKeySegment(keySegment);
                    if(linkEntity != null)
                    {
                        builder = builder.appendNavigationSegment(linkEntity).appendRefSegment();
                    }
                }
            }
            if(baseFilters != null && !baseFilters.isEmpty())
            {
                GxURIFilter filter = null;
                for(GxURIFilter other:baseFilters)
                {             
                    other = other.simplify();
                    if(other == GxURIFilterBooleanConst.TRUE)
                        continue;
                    if(other.op == GxURIOp.EQ)
                    {
                        // En las condiciones de tipo Att = parmStr le hago un rtrim al parmStr para soportar el caso
			// de tener una FK desde una tabla SQL hacia una entidad OData. En la tabla SQL la FK queda con espacios al final
                        GxURIFilterRelEq equFilter = (GxURIFilterRelEq)other;
                        if(equFilter.left.arg instanceof GxMember &&
                           equFilter.rightObj instanceof String)
                        {
                            equFilter.rightObj = GXutil.rtrim((String)equFilter.rightObj);
                        }
                    }
                    filter = (filter == null ? other : and(filter, other));
                }
                if(filter != null)
                    builder.filter(filter.build());                        
            }
            URI uri = builder.build();
            if(isCurrentOfUpdate)
            {
                URI editUri = oEntity[0].getEditLink();
                if(editUri.getScheme().equals(uri.getScheme()))
                    uri = editUri;
                else
                {
                    try
                    {
                        uri = new URI(uri.getScheme(), editUri.getUserInfo(), editUri.getHost(), editUri.getPort(), editUri.getPath(), editUri.getQuery(), editUri.getFragment());
                    }catch(URISyntaxException e)
                    {
                        uri = editUri;
                    }
                }
            }
            return uri;
        }finally
        {
            hasKeySegment = (keySegment != null);
            con = null;
            builder = null;
            order = null;
            oEntity = null;
            keySegment = null;
        }
    }
    
    private URIBuilder newURIBuilder()
    {
        return con.client.newURIBuilder(con.url);
    }

    private Map<String, Object> setKeySegmentFromFilters(List<GxURIFilter> filters)
    {
        if(filters == null)
            return null;
        List<GxURIFilter> keyFilters = new ArrayList<>();
        Map<String, Object> keySegment = new HashMap<>();
        for(EdmKeyPropertyRef keyRef : baseType.getKeyPropertyRefs())
        {
            GxURIFilter keyFilter = null;
            String keyName = keyRef.getProperty().getName();
            for(GxURIFilter filter:filters)
            {
                if(filter.op == GxURIOp.EQ)
                {
                    GxURIFilterRelEq equFilter = (GxURIFilterRelEq)filter;
                    GxMember equMember = null;
                    Object value = null;
                    if(equFilter.left.arg instanceof GxMember)
                    {
                        equMember = (GxMember)equFilter.left.arg;
                        value = equFilter.rightObj;
                    }
                    else if(equFilter.right.arg instanceof GxMember)
                    {
                        equMember = (GxMember)equFilter.right.arg;
                        value = equFilter.leftObj;
                    }
                    if(equMember != null && keyName.equals(equMember.memberName))
                    {
                        keyFilter = filter;
                        if(value instanceof String)
                            value = Encoder.encode((String)value);
                        keySegment.put(equMember.memberName, value);
                        break;
                    }
                }
            }
            if(keyFilter == null)
                return null; // No pude instanciar la clave
            keyFilters.add(keyFilter);                
        }
        filters.removeAll(keyFilters);
        return keySegment;
    }
    
    
    private ClientProperty newProperty(ClientObjectFactory objFactory, String key, Object value, EdmType type)
    {
        switch(type.getKind())
        {
            case PRIMITIVE:
            {
                ClientPrimitiveValue.Builder builder = objFactory.newPrimitiveValueBuilder();
                builder.setType(type);
                if(value != null && type instanceof EdmGeographyPoint)
                {
                    Point geoPoint;
                    try {
                        geoPoint = EdmGeographyPoint.getInstance().valueOfString(String.format("geography'SRID=0;%s'", value.toString().replace(" (", "(")), null, null, null, null, null, Point.class);
                    } catch (EdmPrimitiveTypeException ex) 
                    {
                        geoPoint = new Point(Geospatial.Dimension.GEOGRAPHY, SRID.valueOf("0"));
                        geoPoint.setX(0);
                        geoPoint.setY(0);
                        geoPoint.setZ(0);
                    }
                    value = geoPoint;
                }
                setsNullToNonStringType |= (value == null && !(type instanceof EdmString)); // @hack para resolver bug Olingo 4.5 con nulos y tipo de dato <> string
                builder.setValue(value);
                return objFactory.newPrimitiveProperty(key, builder.build());
            }
            case ENUM:
            {
                setsNullToNonStringType |= (value == null);
                return objFactory.newEnumProperty(key, objFactory.newEnumValue(type.getFullQualifiedName().getFullQualifiedNameAsString(), value != null ? con.toEnumValue((EdmEnumType)type, (int)value) : null));
            }
            case COMPLEX:
            {
                setsNullToNonStringType |= (value == null);
                return objFactory.newComplexProperty(key, (ClientComplexValue)value);
            }
            default:
                throw new UnsupportedOperationException("Not supported yet : " + value.getClass().toString() + " - " + value.toString());
        }
    }

    private ClientObjectFactory getObjFactory()
    {
        return con.client.getObjectFactory();
    }    
    
    private String getEntityAndName(String entity, String name)
    {
        if(entity != null)
        {
            String entityName = entity;
            for(EdmEntityType entityType:entities)
            {
                String mappedEntity = con.entity(entityType, entity);
                entityName = mappedEntity != null ? mappedEntity : entity;
                EdmNavigationProperty navProp = entityType.getNavigationProperty(entityName);
                if(navProp != null && navProp.isCollection())
                    return null; // Aqui se podria poner el orden parcial dentro de la collection
                else break;
            }                        
            return String.format("%s/%s", entityName, name);
        }
        else return name;
    }
    
    // Filtros
    private FilterFactory getFilterFactory()
    {
        return con.client.getFilterFactory();
    }
    
    private FilterArgFactory getArgFactory()
    {
        return con.client.getFilterFactory().getArgFactory();
    }    
    
    public GxURIFilterLogical and(GxURIFilter left, GxURIFilter right)
    {
        FilterFactory filterFactory = getFilterFactory();
        return logical(GxURIOp.AND, filterFactory.and(left, right), left, right);
    }

    public GxURIFilterLogical or(GxURIFilter left, GxURIFilter right)
    {
        FilterFactory filterFactory = getFilterFactory();
        return logical(GxURIOp.OR, filterFactory.or(left, right), left, right);
    }
    
    public GxURIFilterLogical not(GxURIFilter filter)
    {
        FilterFactory filterFactory = getFilterFactory();
        return new GxURIFilterLogicalUnary(GxURIOp.NOT, filterFactory.not(filter), filter);
    }
        
    public GxFilterArg member(String memberName)
    {
        return member(null, memberName);
    }
    
    private HashSet<String> currentFilterEntities = null;
    public GxFilterArg member(String entity, String memberName)
    {
        if(currentFilterEntities == null)
            currentFilterEntities = new HashSet<>();
        currentFilterEntities.add(entity);
        return new GxFilterArgEntity(new GxMember(getArgFactory(), memberName), entity);
    }
    
    @Deprecated
    public GxFilterArg castEnum(String memberName)
    {
        return castEnum(member(memberName));
    }
    
    public GxFilterArg castEnum(GxFilterArg member)
    {
        FilterArgFactory argFactory = getArgFactory();
        return new GxFilterArgEntity(argFactory.cast(member, argFactory.literal("Edm.String")), null);
    }    
    
    public Object minvalue(String sType)
    {
        switch(sType)
        {
            case "Time": // Chequear en la metadata si se soporta la funcion mindatetime()
            case "Date":
                return getNullDate();
            case "Guid":
                return UUID.fromString("00000000-0000-0000-0000-000000000000");
            default:
                 return new GxFilterArgConst("null");
        }
    }

    public GxFilterArg getFilterArg(FilterArgFactory argFactory, Object arg)
    {
        if(arg instanceof GxFilterArg)
            return (GxFilterArg)arg;
        else if(arg instanceof FilterArg)
            return new GxFilterArg((FilterArg)arg);
        else if(arg instanceof java.sql.Timestamp) // Olingo No esta generando bien las constantes timestamp
        {
            try
            {
                FilterArg literal = argFactory.literal(arg);
                return new GxFilterArgConst(URLDecoder.decode(literal.build(), Charset.defaultCharset().name()), literal instanceof FilterLiteral ? (FilterLiteral)literal : null);
            }catch(UnsupportedEncodingException e)
            {
                return new GxFilterArg(argFactory.literal(arg));
            }
        }
        else return new GxFilterArg(argFactory.literal(arg));
    }
    
    public GxFilterArg getFilterArg(FilterArgFactory argFactory, Object arg, boolean removeNil)
    {
        if(arg instanceof GxFilterArg)
            return (GxFilterArg)arg;
        else if(arg instanceof FilterArg)
            return new GxFilterArg((FilterArg)arg);
        else return new GxFilterArg(argFactory.literal(removeNil ? arg.toString().replaceAll("%", "") : arg));
    }
    
    public GxFilterArg add(Object left, Object right)
    {
        FilterArgFactory argFactory = getArgFactory();
        GxFilterArg leftFilter = getFilterArg(argFactory, left);
        GxFilterArg rightFilter = getFilterArg(argFactory, right);
        return new GxFilterArgOp(argFactory.add(leftFilter, rightFilter), leftFilter, rightFilter);
    }

    public GxFilterArg sub(Object left, Object right)
    {
        FilterArgFactory argFactory = getArgFactory();        
//Esta mal el codigo del ArgFactory.sub:        return argFactory.sub(getFilterArg(argFactory, left), getFilterArg(argFactory, right));        
        GxFilterArg leftFilter = getFilterArg(argFactory, left);
        GxFilterArg rightFilter = getFilterArg(argFactory, right);
        return new GxFilterArgOp(new FilterOpImpl("sub", leftFilter, rightFilter), leftFilter, rightFilter);
    }

    public GxFilterArg mul(Object left, Object right)
    {
        FilterArgFactory argFactory = getArgFactory();
        GxFilterArg leftFilter = getFilterArg(argFactory, left);
        GxFilterArg rightFilter = getFilterArg(argFactory, right);
        return new GxFilterArgOp(argFactory.mul(leftFilter, rightFilter), leftFilter, rightFilter);
    }

    public GxFilterArg div(Object left, Object right)
    {
        FilterArgFactory argFactory = getArgFactory();
        GxFilterArg leftFilter = getFilterArg(argFactory, left);
        GxFilterArg rightFilter = getFilterArg(argFactory, right);
        return new GxFilterArgOp(argFactory.div(leftFilter, rightFilter), leftFilter, rightFilter);
    }
    
    public GxFilterArg concat(Object left, Object right)
    {
        FilterArgFactory argFactory = getArgFactory();
        GxFilterArg leftFilter = getFilterArg(argFactory, left);
        GxFilterArg rightFilter = getFilterArg(argFactory, right);
        return new GxFilterArgOp(argFactory.concat(leftFilter, rightFilter), leftFilter, rightFilter);
    }

    private GxURIFilterRel relational(GxURIOp op, URIFilter filter, GxFilterArg left, GxFilterArg right)
    {
        return new GxURIFilterRel(op, filter, left, right);
    }
    
    private GxURIFilterLogical logical(GxURIOp op, URIFilter filter, GxURIFilter left, GxURIFilter right)
    {
        return new GxURIFilterLogical(op, filter, left, right);
    }
    
    public GxURIFilterRel eq(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();                
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return new GxURIFilterRelEq(GxURIOp.EQ, filterFactory.eq(leftArg, rightArg), leftArg, rightArg, left, right);
    }

    public GxURIFilterRel ne(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return new GxURIFilterRelEq(GxURIOp.NE, filterFactory.eq(leftArg, rightArg), leftArg, rightArg, left, right);
    }

    public GxURIFilterRel gt(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();                
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return relational(GxURIOp.GT, filterFactory.gt(leftArg, rightArg), leftArg, rightArg);
    }
    
    public GxURIFilterRel ge(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();                
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return relational(GxURIOp.GE, filterFactory.ge(leftArg, rightArg), leftArg, rightArg);
    }

    public GxURIFilterRel lt(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();                
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return relational(GxURIOp.LT, filterFactory.lt(leftArg, rightArg), leftArg, rightArg);
    }

    public GxURIFilterRel le(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();                
        GxFilterArg leftArg = getFilterArg(argFactory, left);
        GxFilterArg rightArg = getFilterArg(argFactory, right);
        return relational(GxURIOp.LE, filterFactory.le(leftArg, rightArg), leftArg, rightArg);
    }

    public GxURIFilterRel contains(Object left, Object right)
    {
        FilterFactory filterFactory = getFilterFactory();
        FilterArgFactory argFactory = filterFactory.getArgFactory();
        GxFilterArg leftArg = getFilterArg(argFactory, left, true);
        GxFilterArg rightArg = getFilterArg(argFactory, right, true);
        return new GxURIFilterRelMatch(filterFactory.match(argFactory.contains(leftArg, rightArg)), leftArg, rightArg);
    }
    
    protected static boolean isDefault(GxFilterArg argObj, Object obj)
    {
        if(obj == null)
            return true;
        if(obj instanceof Number)
            return ((Number)obj).doubleValue() == 0;
        else if(obj instanceof String)
            return obj.equals("");
        else return false;
    }

    private static class ExpandItem
    {
        String entity, entityName;
        String [] select;
        public ExpandItem(String entity, String entityName, String [] select)
        {
            this.entity = entity;
            this.entityName = entityName;
            this.select = select;
        }

        private String getSelect()
        {
            return String.join(", ", select);
        }
    }

    private static class FilterOpImpl implements FilterArg
    {

        String op;
        FilterArg left, right;
        public FilterOpImpl(String op, FilterArg left, FilterArg right)
        {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public String build()
        {
            return new StringBuilder().
                append('(').append(left.build()).
                append(' ').append(op).append(' ').
                append(right.build()).append(')').
                toString();
        }        
    }
    
    private java.sql.Timestamp getNullDate()
    {
        return new java.sql.Timestamp(GXutil.resetTime(GXutil.nullDate()).getTime());
    }
    
    // Funciones
    public GxURIFilter nullvalue(Object obj)
    {
        Object right;
        if(obj instanceof String)
            right = "";
        else if(Number.class.isInstance(obj))
            right = Double.valueOf(0);
        else if(java.util.Date.class.isInstance(obj))
            right = getNullDate();
        else if(obj instanceof UUID)
            right = UUID.fromString("00000000-0000-0000-0000-000000000000");
        else
        {
            right = null;
        }
        return eq(obj, right);
    }

    public static class GxFilterArg implements FilterArg
    {
        FilterArg arg;
        public GxFilterArg(FilterArg arg)
        {
            this.arg = arg;
        }

        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {
            return entitiesSet;
        }

        @Override
        public String build()
        {
            return arg.build();
        }
    }

    public static class GxFilterArgOp extends GxFilterArg
    {
        GxFilterArg left, right;

        private GxFilterArgOp(FilterArg arg, GxFilterArg left, GxFilterArg right)
        {
            super(arg);
            this.left = left;
            this.right = right;
        }

        @Override
        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {            
            return left.fillEntitiesSet(right.fillEntitiesSet(entitiesSet));
        }

        @Override
        public String build()
        {
            return arg.build();
        }
    }

    public static class GxFilterArgEntity extends GxFilterArg
    {
        String entity;
        
        private GxFilterArgEntity(FilterArg arg, String entity)
        {
            super(arg);
            this.entity = entity;
        }

        @Override
        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {
            entitiesSet.add(entity);
            return entitiesSet;
        }
    }

    private static class GxFilterArgConst extends GxFilterArg
    {        
        private final String literal;
        private GxFilterArgConst(String literal)
        {
            this(literal, null);
        }
        
        private GxFilterArgConst(String literal, FilterLiteral filterLiteral)
        {
            super(filterLiteral);
            this.literal = literal;
        }

        @Override
        public String build()
        {
            return literal;
        }        
    }

    private static class GxURIFilterBooleanConst extends GxURIFilter
    {
        protected static GxURIFilterBooleanConst TRUE = new GxURIFilterBooleanConst(true);
        protected static GxURIFilterBooleanConst FALSE = new GxURIFilterBooleanConst(false);
        
        public static GxURIFilterBooleanConst get(boolean boolConst)
        {
            return boolConst ? TRUE : FALSE;
        }
        
        private final boolean boolConst;
        private GxURIFilterBooleanConst(boolean boolConst)
        {
            super(GxURIOp.CONST_BOOLEAN);
            this.boolConst = boolConst;
        }

        @Override
        public String build()
        {
            return boolConst ? "true" : "false";
        }
        
        @Override
        public boolean isAlwaysTrue()
        {
            return boolConst;
        }
        
        @Override
        public boolean isAlwaysFalse()
        {
            return !boolConst;
        }
    }
    
    public enum GxURIOp
    {
        MATCH,
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        AND,
        OR,
        NOT,
        CONST_BOOLEAN
    }
    
    public static abstract class GxURIFilter implements URIFilter
    {
        GxURIOp op;
        public GxURIFilter(GxURIOp op)
        {
            this.op = op;
        }

        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {
            return entitiesSet;
        }
        
        protected GxURIFilter simplify()
        {
            return this;
        }

        protected boolean isAlwaysTrue()
        {
            return false;
        }

        protected boolean isAlwaysFalse()
        {
            return false;
        }
    }
    
    public static class GxURIFilterRel extends GxURIFilter
    {
        URIFilter filter;
        GxFilterArg left, right;
        public GxURIFilterRel(GxURIOp op, URIFilter filter, GxFilterArg left, GxFilterArg right)
        {
            super(op);
            this.filter = filter;
            this.left = left;
            this.right = right;
        }
        
        @Override
        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {            
            return left.fillEntitiesSet(right.fillEntitiesSet(entitiesSet));
        }
        
        @Override
        public String build()
        {
            return filter.build();
        }
    }
    
    public static class GxURIFilterRelMatch extends GxURIFilterRel
    {
        public GxURIFilterRelMatch(URIFilter filter, GxFilterArg left, GxFilterArg right)
        {
            super(GxURIOp.MATCH, filter, left, right);
        }
        
        @Override
        protected GxURIFilter simplify()
        {            
            if(right.arg instanceof FilterLiteral && right.build().equals("''"))
                return GxURIFilterBooleanConst.TRUE;
            else return this;
        }
    }
    
    public static class GxURIFilterRelEq extends GxURIFilterRel
    {
        Object leftObj, rightObj;
        public GxURIFilterRelEq(GxURIOp eqOrNEq, URIFilter filter, GxFilterArg left, GxFilterArg right, Object leftObj, Object rightObj)
        {
            super(eqOrNEq, filter, left, right);
            this.leftObj = leftObj;
            this.rightObj = rightObj;
        }
        
        @Override
        protected GxURIFilter simplify()
        {            
            boolean trueForEq = (op == GxURIOp.EQ);
            if(left.arg instanceof FilterLiteral && right.arg instanceof FilterLiteral)
            {
                if(leftObj != null && rightObj != null)
                {
                    if(leftObj instanceof Comparable && rightObj instanceof Comparable)
                        return GxURIFilterBooleanConst.get(trueForEq == GXutil.compare((Comparable)leftObj, "=", (Comparable)rightObj));
                    else return GxURIFilterBooleanConst.get(trueForEq == leftObj.equals(rightObj));
                }
                else return GxURIFilterBooleanConst.get(trueForEq == (leftObj != null ? isDefault(left, leftObj) : isDefault(right, rightObj)));
            }
            return this;
        }
    }

    public static class GxURIFilterLogical extends GxURIFilter
    {
        URIFilter filter;
        GxURIFilter left, right;

        public GxURIFilterLogical(GxURIOp op, URIFilter filter, GxURIFilter left, GxURIFilter right)
        {
            super(op);
            this.filter = filter;
            this.left = left;
            this.right = right;
        }
        
        @Override
        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {            
            return left.fillEntitiesSet(right.fillEntitiesSet(entitiesSet));
        }

        @Override
        public String build()
        {
            return filter.build();
        }
        
        @Override
        protected GxURIFilter simplify()
        {
            if(left != null)
                left = left.simplify();
            if(right != null)
                right = right.simplify();
            switch (op)
            {
                case AND:
                    if(left.isAlwaysFalse() || right.isAlwaysFalse())
                        return GxURIFilterBooleanConst.FALSE;
                    if(left.isAlwaysTrue())
                        return right.isAlwaysTrue() ? GxURIFilterBooleanConst.TRUE : right;
                    else if(right.isAlwaysTrue())
                        return left;
                    break;
                case OR:
                    if(left.isAlwaysTrue() || right.isAlwaysTrue())
                        return GxURIFilterBooleanConst.TRUE;
                    if(left.isAlwaysFalse())
                        return right.isAlwaysFalse() ? GxURIFilterBooleanConst.FALSE : right;
                    else if(right.isAlwaysFalse())
                        return left;
                    break;
                case NOT:
                    if(left.isAlwaysFalse())
                        return GxURIFilterBooleanConst.TRUE;
                    else if(left.isAlwaysTrue())                            
                        return GxURIFilterBooleanConst.FALSE;
                    break;
                default:
                    break;
            }
            return this;
        }
    }

    private static class GxURIFilterLogicalUnary extends GxURIFilterLogical
    {
        public GxURIFilterLogicalUnary(GxURIOp op, URIFilter filter, GxURIFilter arg)
        {
            super(op, filter, arg, null);
        }        

        @Override
        public Set<String> fillEntitiesSet(Set<String> entitiesSet)
        {            
            return left.fillEntitiesSet(entitiesSet);
        }
    }
    
    private static class GxMember implements FilterArg
    {
        String memberName;
        FilterArgFactory argFactory;

        public GxMember(FilterArgFactory argFactory, String memberName)
        {
            this.memberName = memberName.replace('.', '/');
            this.argFactory = argFactory;
        }

        @Override
        public String build()
        {
            return argFactory.property(memberName).build();
        }
    }
    
}
