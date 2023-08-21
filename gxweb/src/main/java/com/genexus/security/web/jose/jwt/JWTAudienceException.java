package com.genexus.security.web.jose.jwt;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents Exception related to Audience - for example illegal audience on JWT Verification
 */
public class JWTAudienceException extends JWTVerifyException {

    private JsonNode audienceNode;

    public JWTAudienceException(final JsonNode audienceNode) {
        this.audienceNode = audienceNode;
    }

    public JWTAudienceException(final String message, final JsonNode audienceNode) {
        super(message);
        this.audienceNode = audienceNode;
    }

    public List<String> getAudience() {
        final ArrayList<String> audience = new ArrayList<String>();
        if (audienceNode.isArray()) {
            for (final JsonNode jsonNode : audienceNode) {
                audience.add(jsonNode.textValue());
            }
        } else if (audienceNode.isTextual()) {
            audience.add(audienceNode.textValue());
        }
        return audience;
    }
}
