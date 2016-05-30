package org.rabix.engine.model.scatter;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.protocol.draft2.bean.Draft2EmbeddedApp;
import org.rabix.engine.model.scatter.impl.ScatterCartesianStrategy;
import org.rabix.engine.model.scatter.impl.ScatterZipStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "scatterMethod", defaultImpl = Draft2EmbeddedApp.class)
@JsonSubTypes({ 
    @Type(value = ScatterZipStrategy.class, name = "CommandLineTool"),
    @Type(value = ScatterCartesianStrategy.class, name = "ExpressionTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ScatterStrategy {

  ScatterMethod getScatterMethod();
  
}
