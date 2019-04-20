package org.onap.so.adapters.catalogdb.catalogrest;

import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "groups")
public class QueryGroups extends CatalogQuery{

    private List<VFCInstanceGroup> vfcInstanceGroups;
    private static final String TEMPLATE =
            "\n"+
                    "\t{ \"modelInfo\"                    : {\n"+
                    "\t\t\"modelName\"              : <MODEL_NAME>,\n"+
                    "\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
                    "\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
                    "\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
                    "\t\t},\n"+
                    "<_VNFCS_>\n" +
                    "\t}";

    public QueryGroups() {
        super();
        vfcInstanceGroups = new ArrayList<>();
    }

    public QueryGroups(List<VFCInstanceGroup> vfcInstanceGroups) {
        this.vfcInstanceGroups = new ArrayList<>();
        if (vfcInstanceGroups != null) {
            for (VFCInstanceGroup g: vfcInstanceGroups) {
                if (logger.isDebugEnabled()) {
                    logger.debug(g.toString());
                }
                this.vfcInstanceGroups.add(g);
            }
        }
    }

    public List<VFCInstanceGroup> getVfcInstanceGroups() {
        return vfcInstanceGroups;
    }

    public void setVfcInstanceGroups(List<VFCInstanceGroup> vfcInstanceGroups) {
        this.vfcInstanceGroups = vfcInstanceGroups;
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray)
            sb.append("{ ");
        if (isArray)
            sb.append("\"groups\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (VFCInstanceGroup o : vfcInstanceGroups) {
            if (first)
                sb.append("\n");
            first = false;

            // require vnfc customization
            boolean vfcNull = o.getVnfcInstanceGroupCustomizations().get(0).getVnfcCustomizations() == null ? true : false;

            put(valueMap, "MODEL_NAME", o.getModelName());
            put(valueMap, "MODEL_UUID", o.getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", o.getModelUUID());
            put(valueMap, "MODEL_VERSION", o.getModelUUID());

            String subItem = new QueryVnfcs(vfcNull ? null
                    : o.getVnfcInstanceGroupCustomizations().get(0).getVnfcCustomizations())
                    .JSON2(true, true);
            valueMap.put("_VNFCS_", subItem.replaceAll("(?m)^", "\t\t"));
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
