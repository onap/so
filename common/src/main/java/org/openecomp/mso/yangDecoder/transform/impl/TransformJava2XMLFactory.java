package org.openecomp.mso.yangDecoder.transform.impl;

import javassist.ClassPool;
import org.openecomp.mso.yangDecoder.base.TYangJsonXmlBase;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * Created by 10112215 on 2017/3/26.
 */
public class TransformJava2XMLFactory extends TYangJsonXmlBase {
    BindingToNormalizedNodeCodec mappingservice;
    ModuleInfoBackedContext moduleInfoBackedContext;
    protected final static ControllerContext controllerContext = ControllerContext.getInstance();

    public TransformJava2XMLServiceImpl getJava2xmlService() {
        if (java2xmlService == null) {
            try {
                setup2();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return java2xmlService;
    }

    TransformJava2XMLServiceImpl java2xmlService;

    @Override
    protected void setupWithDataBroker(final DataBroker dataBroker) {
        // Intentionally left No-op, subclasses may customize it
        mappingservice = new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault()))));
        moduleInfoBackedContext = ModuleInfoBackedContext.create();
        // moduleInfoBackedContext.registerModuleInfo(BindingReflections.getModuleInfo(SncTunnels.class));
        try {
            for (YangModuleInfo yangModuleInfo : getModuleInfos()) {
                moduleInfoBackedContext.registerModuleInfo(yangModuleInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        schemaContext = moduleInfoBackedContext.tryToCreateSchemaContext().get();
        mappingservice.onGlobalContextUpdated(schemaContext);
        controllerContext.setSchemas(schemaContext);

    }

    public final void setup2() throws Exception {
        super.setup();
        if(java2xmlService==null)
            java2xmlService = new TransformJava2XMLServiceImpl(mappingservice, schemaContext);
    }


}
