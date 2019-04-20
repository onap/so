package org.onap.so.adapters.catalogdb.catalogrest;

import org.onap.so.db.catalog.beans.VnfcCustomization;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "vnfcs")
public class QueryVnfcs extends CatalogQuery{
    private List<VnfcCustomization> vnfcCustomizations;
    private static final String TEMPLATE =
            "\t{\n"+
                    "\t\t\"modelInfo\"               : { \n"+
                    "\t\t\t\"modelName\"              : <MODEL_NAME>,\n"+
                    "\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
                    "\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
                    "\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
                    "\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>\n"+
                    "\t\t}"+
                    "\t}";

    public QueryVnfcs() {
        super();
        vnfcCustomizations = new ArrayList();
    }

    public QueryVnfcs(List<VnfcCustomization> vnfcCustomizations) {
        this.vnfcCustomizations = new ArrayList();
        if (vnfcCustomizations != null) {
            for (VnfcCustomization vnfcCustomization : vnfcCustomizations) {
                if (logger.isDebugEnabled()) {
                    logger.debug(vnfcCustomization.toString());
                }
                this.vnfcCustomizations.add(vnfcCustomization);
            }
        }
    }

    public List<VnfcCustomization> getVnfcCustomizations() {
        return vnfcCustomizations;
    }

    public void setVnfcCustomizations(List<VnfcCustomization> vnfcCustomizations) {
        this.vnfcCustomizations = vnfcCustomizations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (VnfcCustomization o: vnfcCustomizations) {
            sb.append(i).append("\t");
            if (!first) {
                sb.append("\n");
            }
            first = false;
            sb.append(o);
        }
        return sb.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray) {
            sb.append("{");
        }

        if (isArray) {
            sb.append("\"vnfcs\": [");
        }

        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for(VnfcCustomization o: vnfcCustomizations) {
            if (first)
                sb.append("\n");
            first = false;

            put(valueMap, "MODEL_NAME",               o.getModelName());
            put(valueMap, "MODEL_UUID",               o.getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID",       o.getModelInvariantUUID());
            put(valueMap, "MODEL_VERSION",            o.getModelVersion());
            put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUUID());

            sb.append(sep).append(this.setTemplate(TEMPLATE, valueMap));
            sep = ",\n";
        }
        if (!first)
            sb.append("\n");
        if (isArray)
            sb.append("]");
        if (!isEmbed && isArray)
            sb.append("}");
        return sb.toString();
    }
}
