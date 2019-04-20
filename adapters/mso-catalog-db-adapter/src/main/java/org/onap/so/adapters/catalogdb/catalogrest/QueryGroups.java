package org.onap.so.adapters.catalogdb.catalogrest;

import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "groups")
public class QueryGroups extends CatalogQuery {

    private List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations;
    private static final String TEMPLATE = "\n" + "\t{ \"modelInfo\"                    : {\n"
            + "\t\t\"modelName\"              : <MODEL_NAME>,\n" + "\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
            + "\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
            + "\t\t\"modelVersion\"           : <MODEL_VERSION>,\n" + "\t\t},\n" + "<_VNFCS_>\n" + "\t}";

    public QueryGroups() {
        super();
        vnfcInstanceGroupCustomizations = new ArrayList<>();

    }

    public QueryGroups(List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations) {
        this.vnfcInstanceGroupCustomizations = new ArrayList<>();
        if (vnfcInstanceGroupCustomizations != null) {
            for (VnfcInstanceGroupCustomization g : vnfcInstanceGroupCustomizations) {
                if (logger.isDebugEnabled()) {
                    logger.debug(g.toString());
                }
                this.vnfcInstanceGroupCustomizations.add(g);
            }
        }
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

        for (VnfcInstanceGroupCustomization o : vnfcInstanceGroupCustomizations) {
            if (first)
                sb.append("\n");
            first = false;

            boolean vnfcCustomizationNull = o.getVnfcCustomizations() == null;
            InstanceGroup instanceGroup = o.getInstanceGroup();


            put(valueMap, "MODEL_NAME", instanceGroup.getModelName());
            put(valueMap, "MODEL_UUID", instanceGroup.getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", instanceGroup.getModelUUID());
            put(valueMap, "MODEL_VERSION", instanceGroup.getModelUUID());

            String subItem = new QueryVnfcs(vnfcCustomizationNull ? null : o.getVnfcCustomizations()).JSON2(true, true);
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
