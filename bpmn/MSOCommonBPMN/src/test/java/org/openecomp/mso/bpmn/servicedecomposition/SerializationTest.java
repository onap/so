package org.openecomp.mso.bpmn.servicedecomposition;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Test;
import org.openecomp.mso.SerializableChecker;
import org.openecomp.mso.SerializableChecker.SerializationFailure;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.common.DelegateExecutionImpl;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationTest {
	
	private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";
	private static final String FLOW_VAR_NAME = "flowToBeCalled";
	private static final String LOOKUP_KEY_MAP_VAR_NAME = "lookupKeyMap";
	private static final String GBB_INPUT_VAR_NAME = "gBBInput";
	protected ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testSerializationOfAllPojos() throws IOException {
		GeneralBuildingBlock gbb = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		DelegateExecution execution = new ExecutionImpl();
		execution.setVariable(FLOW_VAR_NAME, "AssignServiceInstanceBB");
		execution.setVariable(GBB_INPUT_VAR_NAME, gbb);
		execution.setVariable(LOOKUP_KEY_MAP_VAR_NAME, lookupKeyMap);
		System.out.println(execution.getVariables());
		BuildingBlockExecution gBuildingBlockExecution = new DelegateExecutionImpl(execution);
		boolean isSerializable = SerializationTest.isSerializable(gBuildingBlockExecution);
		assertEquals(true, isSerializable);
	}
	
	public static boolean isSerializable(final Object o)
    {
        final boolean retVal;

        if(implementsInterface(o))
        {
            retVal = attemptToSerialize(o);
        }
        else
        {
            retVal = false;
        }

        return (retVal);
    }

    private static boolean implementsInterface(final Object o)
    {
        final boolean retVal;

        retVal = ((o instanceof Serializable) || (o instanceof Externalizable));

        return (retVal);
    }

    private static boolean attemptToSerialize(final Object o)
    {
        final OutputStream sink;
        ObjectOutputStream stream;

        stream = null;

        try
        {
            sink   = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(sink);
            stream.writeObject(o);
            // could also re-serilalize at this point too
        }
        catch(final IOException ex)
        {
            return (false);
        }
        finally
        {
            if(stream != null)
            {
                try
                {
                    stream.close();
                }
                catch(final IOException ex)
                {
                    // should not be able to happen
                }
            }
        }

        return (true);
    }
}
