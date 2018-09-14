package org.onap.so.openstack.utils;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MsoMulticloudParam {

    @JsonProperty("generic-vnf-id")
    private String genericVnfId;

    @JsonProperty("vf-module-id")
    private String vfModuleId;

    @JsonProperty("oof_directives")
    private String oofDirectives;

    @JsonProperty("sdnc_directives")
    private String sdncDirectives;

    @JsonProperty("template_type")
    private String templateType;

    @JsonProperty("template_data")
    private String templateData;

    public void setGenericVnfId(String genericVnfId){
        this.genericVnfId = genericVnfId;
    }

    public String getGenericVnfId(){
        return this.genericVnfId;
    }

    public void setVfModuleId(String vfModuleId){
        this.vfModuleId = vfModuleId;
    }

    public String getVfModuleId(){
        return this.vfModuleId;
    }

    public void setOofDirectives(String oofDirectives){
        this.oofDirectives = oofDirectives;
    }

    public String getOofDirectives(){
        return this.oofDirectives;
    }

    public void setSdncDirectives(String sdncDirectives){
        this.sdncDirectives = sdncDirectives;
    }

    public String getSdncDirectives(){
        return this.sdncDirectives;
    }

    public void setTemplateType(String templateType){
        this.templateType = templateType;
    }

    public String TemplateType(){
        return this.templateType;
    }

    public void setTemplateData(String templateData){
        this.templateData = templateData;
    }

    public String getTemplateData(){
        return this.templateData;
    }

    @Override
    public String toString() {
        return String.format("MulticloudParam{"
                + "genericVnfId='%s',"
                + " vfModuleId='%s',"
                + " oofDirectives='%s',"
                + " sdncDirectives='%s',"
                + " templateType='%s',"
                + " templateData='%s'"
                + "}",
            genericVnfId,
            vfModuleId,
            oofDirectives,
            sdncDirectives,
            templateType,
            templateData);
    }
}
