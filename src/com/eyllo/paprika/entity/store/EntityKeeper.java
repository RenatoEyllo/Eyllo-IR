/**
 * 
 */
package com.eyllo.paprika.entity.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.Entity;
import com.eyllo.paprika.html.parser.ParseUtils;

/**
 * @author renatomarroquin
 *
 */
public class EntityKeeper {

    private List<Entity> entities;
    
    /**
     * Logger to help us write write info/debug/error messages
     */
    private static Logger LOGGER = LoggerFactory.getLogger(EntityKeeper.class);
    
    public void exportToJson(String pPath){
        if (entities != null && entities.size() > 0)
            ParseUtils.writeJsonFile(entities, pPath);
        else
            LOGGER.info("Entities haven't been retrieved");
    }
}
