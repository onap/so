package org.openecomp.mso.bpmn.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openecomp.mso.logger.MsoLogger;

/**
 * Sets up the unit test (H2) database for Camunda.
 */
public class CamundaDBSetup {
	private static boolean isDBConfigured = false;
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	
	private CamundaDBSetup() {
	    /**
	     * Constructor.
	     */
	}
	
	public static synchronized void configure() throws SQLException {
		if (isDBConfigured) {
			return;
		}

		LOGGER.debug ("Configuring the Camunda H2 database for MSO");

		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = DriverManager.getConnection(
				"jdbc:h2:mem:camunda;DB_CLOSE_DELAY=-1", "sa", "");

			stmt = connection.prepareStatement("delete from ACT_HI_VARINST");
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

			stmt = connection.prepareStatement("ALTER TABLE ACT_HI_VARINST alter column TEXT_ clob");
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			
//			stmt = connection.prepareStatement("ALTER TABLE ACT_HI_VARINST alter column NAME_ clob");
//			stmt.executeUpdate();
//			stmt.close();
//			stmt = null;

			stmt = connection.prepareStatement("delete from ACT_HI_DETAIL");
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

			stmt = connection.prepareStatement("ALTER TABLE ACT_HI_DETAIL alter column TEXT_ clob");
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

//			stmt = connection.prepareStatement("ALTER TABLE ACT_HI_DETAIL alter column NAME_ clob");
//			stmt.executeUpdate();
//			stmt.close();
//			stmt = null;

			stmt = connection.prepareStatement("ALTER TABLE ACT_RU_VARIABLE alter column TEXT_ clob");
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

			connection.close();
			connection = null;

			isDBConfigured = true;
		} catch (SQLException e) {
		    LOGGER.debug ("CamundaDBSetup caught " + e.getClass().getSimpleName());
			LOGGER.debug("SQLException :",e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
				}
			}
		}
	}
}