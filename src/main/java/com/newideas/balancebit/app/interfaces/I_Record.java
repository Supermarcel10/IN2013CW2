package com.newideas.balancebit.app.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface I_Record {
	String getExecuteSql();

	String getSelectSql();

	Boolean handleResult(ResultSet rs);

	void construct(ResultSet rs) throws SQLException;
}