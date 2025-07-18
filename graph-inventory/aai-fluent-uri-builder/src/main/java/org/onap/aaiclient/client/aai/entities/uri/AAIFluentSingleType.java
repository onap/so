package org.onap.aaiclient.client.aai.entities.uri;

import org.onap.aaiclient.client.graphinventory.GraphInventoryFluentType;
import org.onap.aaiclient.client.aai.AAIObjectName;
import org.onap.aaiclient.client.aai.AAIObjectType;
import com.google.common.base.CaseFormat;

public interface AAIFluentSingleType extends GraphInventoryFluentType<AAIObjectType> {

    public interface Info extends GraphInventoryFluentType.Info, AAIObjectName {

        public default String typeName() {
            return this.getName();
        }

        public default String typeName(CaseFormat format) {
            return CaseFormat.LOWER_HYPHEN.to(format, this.getName());
        }

        public interface UriParams extends GraphInventoryFluentType.Info.UriParams {

        }

    }
}
