package com.genexus.db.odata;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.edm.xml.ClientCsdlAnnotation;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;

public class ModelInfo
{
    private static final HashMap<String, ModelInfo> serviceModels = new HashMap<>();

    static ModelInfo getModel(String connUrl)
    {
        return serviceModels.get(connUrl);
    }
    
    static void addModel(String connUrl, ModelInfo modelInfo)
    {
        serviceModels.put(connUrl, modelInfo);
    }

    final String url;
    final Edm model;
    boolean useChunked;
    HttpClientFactory handlerFactory = null;
    ModelInfo(String url, Edm model, String checkOptimisticConcurrency)
    {
       this.url = url;
       this.model = model;
       initializeOptimisticConcurrency(checkOptimisticConcurrency);
       initializeEntityMapper(model);
    }
    
    public Edm getModel()
    {
        return model;
    }

    static final HashMap<String, HashMap<EdmEntityType, HashMap<String, String>>> entityMappers = new HashMap<>();
    private static final HashMap<String, String> NIL_MAPPER = new HashMap<>();
    private void initializeEntityMapper(Edm model)
    {
		HashMap<EdmEntityType, HashMap<String, String>> entityMapper = entityMappers.computeIfAbsent(url, k -> new HashMap<>());
		HashMap<String, String> rootMapper = entityMapper.computeIfAbsent(null, k -> new HashMap<>());
		EdmEntityContainer container = model.getEntityContainer();
        HashMap<EdmEntityType, List<String>> entitySetTypes = new HashMap<>();
        for(EdmEntitySet entitySet:container.getEntitySets())
        {
			List<String> entitySets = entitySetTypes.computeIfAbsent(entitySet.getEntityType(), k -> new ArrayList<>());
			entitySets.add(entitySet.getName());
        }
        for(EdmEntitySet entitySet:container.getEntitySets())
        {
            EdmEntityType type = entitySet.getEntityType();
            initializeEntityMapper(entityMapper, type, entitySetTypes);
            rootMapper.put(entitySet.getName(), type.getName());
            if(!needsCheckOptimisticConcurrencyAll)
            {
                for(EdmAnnotation annotation:entitySet.getAnnotations())
                { // @hack intento obtener si la actualizacion de este entitySet requiere IfMatch
                    try
                    {
                        Field annotationField = annotation.getClass().getDeclaredField("annotation");
                        annotationField.setAccessible(true);
                        ClientCsdlAnnotation csdlAnnotation = (ClientCsdlAnnotation)annotationField.get(annotation);
                        if(csdlAnnotation.getTerm().endsWith("OptimisticConcurrency"))
                        {
                            if(checkOptimisticConcurrencyEntities == null)
                                checkOptimisticConcurrencyEntities = new HashSet<>();
                            checkOptimisticConcurrencyEntities.add(entitySet.getName().toLowerCase());
                        }
                    }catch(IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ignored)
                    {

                    }
                }
            }
        }
    }

    public void initializeEntityMapper(HashMap<EdmEntityType, HashMap<String, String>> entityMapper, EdmEntityType type, HashMap<EdmEntityType, List<String>> entitySetTypesMap)
    {
		HashMap<String, String> currentMapper = entityMapper.computeIfAbsent(type, k -> new HashMap<>());
		for(String navPropName : type.getNavigationPropertyNames())
        {
            EdmNavigationProperty navProp = type.getNavigationProperty(navPropName);
            EdmEntityType navPropType = navProp.getType();
            if(!currentMapper.containsKey(navProp.getName()))
            {
                currentMapper.put(navProp.getName(), navProp.getName());  // Navego por el nombre de la propiedad
                if(entitySetTypesMap.containsKey(navPropType))
                { // Si es un entity set en gx se ve como tal
                    for(String entitySetName:entitySetTypesMap.get(navPropType))
                        currentMapper.put(entitySetName, navPropName);                        
                }else
                { // si no es un enityset en gx se ve con el tipo de la entidad
                    currentMapper.put(navPropType.getName(), navProp.getName());
                }
                if(!navPropType.equals(type))
                    initializeEntityMapper(entityMapper, navPropType, entitySetTypesMap);
            }
        }        
    }        

    private void initializeOptimisticConcurrency(String checkOptimisticConcurrency)
    {
        if(checkOptimisticConcurrency != null)
        {
            checkOptimisticConcurrency = checkOptimisticConcurrency.trim().toLowerCase();
            if(checkOptimisticConcurrency.equals("true") || checkOptimisticConcurrency.equals("all"))
                needsCheckOptimisticConcurrencyAll = true;
            else
            {
                checkOptimisticConcurrencyEntities = new HashSet<>();
                for(String entity:checkOptimisticConcurrency.split(","))
                {
                    checkOptimisticConcurrencyEntities.add(entity.trim());
                }
            }
        }
    }

    private boolean needsCheckOptimisticConcurrencyAll = false;
    private HashSet<String> checkOptimisticConcurrencyEntities = null;
    boolean needsCheckOptimisticConcurrency(URI updURI)
    {
        try
        {
            if(needsCheckOptimisticConcurrencyAll)
                return true;
            if(checkOptimisticConcurrencyEntities != null)
            {
                String path = updURI.getPath().toLowerCase();
                path = path.substring(path.lastIndexOf('/')+1);
                if(path.contains("("))
                    path = path.substring(0, path.indexOf('('));
                return checkOptimisticConcurrencyEntities.contains(path);
            }                
        }catch(Exception ignored){}
        return false;
    }    
    
    public String entity(String name)
    {
        HashMap<EdmEntityType, HashMap<String, String>> entityMapper = entityMappers.get(url);
        return entityMapper == null ? name : entityMapper.getOrDefault(null, NIL_MAPPER).getOrDefault(name, name);
    }

    public String entity(EdmEntityType fromEntity, String name)
    {
        HashMap<EdmEntityType, HashMap<String, String>> entityMapper = entityMappers.get(url);
        return entityMapper == null ? name : entityMapper.getOrDefault(fromEntity, NIL_MAPPER).get(name);
    }
    
}
