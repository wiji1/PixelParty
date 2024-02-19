package dev.wiji.pixelparty.sql;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLTable {
	public TableStructure structure;
	public ConnectionInfo connectionInfo;
	public String tableName;
	public Connection connection;

	public SQLTable(ConnectionInfo connectionInfo, String tableName, TableStructure structure) {
		if(TableManager.getTable(tableName) != null) throw new RuntimeException("Table already exists");

		this.structure = structure;
		this.connectionInfo = connectionInfo;
		this.tableName = tableName;

		this.connection = connectionInfo.getConnection();
		structure.build(this);

		TableManager.registerTable(this);
	}

	public void executeUpdate(String query) {
		try {
			PreparedStatement stmt = connection.prepareStatement(query);
			executeUpdate(stmt);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void executeUpdate(PreparedStatement statement) {
		try {
			statement.executeUpdate();
		} catch(Exception e) {
			System.out.println(statement.toString());
			throw new RuntimeException(e);
		}
	}

	public ResultSet executeQuery(String query) {
		try {
			return executeQuery(connection.prepareStatement(query));
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ResultSet executeQuery(PreparedStatement statement) {
		try {
			return statement.executeQuery();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insertRow(Value... values) {
		List<String> columnStrings = new ArrayList<>();
		for(TableColumn column : structure.columns) {
			columnStrings.add(column.type.getSimpleName() + " " + column.name);
		}

		if(values.length != structure.columns.size()) throw new RuntimeException("Invalid number of values");

		StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " VALUES (");

		System.out.println("------------------------");
		for(int i = 0; i < values.length; i++) {
			System.out.println(values[i].fieldName + " " + values[i].value.getClass() + " " + structure.columns.get(i).type);
		}
		System.out.println("------------------------");

		for(int i = 0; i < values.length; i++) {

			if(values[i].value.getClass() != structure.columns.get(i).type)
				throw new RuntimeException("\nCorrect values: " + String.join(", ", columnStrings));

			query.append("?");
			if(i != values.length - 1) query.append(", ");
		}
		query.append(")");

		try {
			PreparedStatement stmt = connection.prepareStatement(query.toString());
			for(int i = 0; i < values.length; i++) {
				stmt.setObject(i + 1, values[i].value);
			}
			executeUpdate(stmt);
		} catch(Exception e) {
			throw new RuntimeException(e + "\nCorrect values: " + String.join(", ", columnStrings));
		}
	}

	public void updateRow(QueryStorage... storage) {

		List<Constraint> constraints = new ArrayList<>();
		List<Value> values = new ArrayList<>();
		for(QueryStorage queryStorage : storage) {
			if(queryStorage instanceof Constraint) {
				constraints.add((Constraint) queryStorage);
			} else if(queryStorage instanceof Value) {
				values.add((Value) queryStorage);
			} else {
				throw new RuntimeException("Invalid QueryStorage type. Must be Constraint or Value");
			}
		}


		try {
			ResultSet rs = selectRow(constraints.toArray(new Constraint[0]));
			if(!rs.next()) {
				List<QueryStorage> insertStorage = new ArrayList<>();

				for(Constraint constraint : constraints) {
					insertStorage.add(new Value(constraint.fieldName, constraint.value));
				}

				insertStorage.addAll(values);

				insertRow(insertStorage.toArray(new Value[0]));
				rs.close();
				return;
			}

			rs.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");
		for(Value value : values) {
			query.append(value.fieldName).append(" = ?");
			if(values.indexOf(value) != values.size() - 1) query.append(", ");
		}

		if(constraints.size() > 0) query.append(" WHERE ");
		constraint(constraints, query);

		try {
			PreparedStatement stmt = connection.prepareStatement(query.toString());
			for(int i = 0; i < values.size(); i++) {
				stmt.setObject(i + 1, values.get(i).value);
			}
			for(int i = 0; i < constraints.size(); i++) {
				stmt.setObject(i + 1 + values.size(), constraints.get(i).value);
			}

			executeUpdate(stmt);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteRow(Constraint... storage) {
		List<Constraint> constraints = Arrays.asList(storage);

		StringBuilder query = new StringBuilder("DELETE FROM " + tableName);
		if(constraints.size() > 0) query.append(" WHERE ");
		constraint(constraints, query);

		try {
			PreparedStatement stmt = connection.prepareStatement(query.toString());
			for(int i = 0; i < constraints.size(); i++) {
				stmt.setObject(i + 1, constraints.get(i).value);
			}

			executeUpdate(stmt);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ResultSet selectRow(QueryStorage... storage) {
		List<Constraint> constraints = new ArrayList<>();
		List<Field> fields = new ArrayList<>();
		for(QueryStorage queryStorage : storage) {
			if(queryStorage instanceof Constraint) {
				constraints.add((Constraint) queryStorage);
			} else if(queryStorage instanceof Field) {
				fields.add((Field) queryStorage);
			} else {
				throw new RuntimeException("Invalid QueryStorage type. Must be Constraint or Field");
			}
		}


		StringBuilder query = new StringBuilder("SELECT ");
		if(fields.size() == 0) query.append("*");
		else {
			for(Field field : fields) {
				query.append(field.fieldName);
				if(fields.indexOf(field) != fields.size() - 1) query.append(", ");
			}
		}
		query.append(" FROM ").append(tableName);

		if(constraints.size() > 0) query.append(" WHERE ");
		constraint(constraints, query);

		try {
			PreparedStatement stmt = connection.prepareStatement(query.toString());
			for(int i = 0; i < constraints.size(); i++) {
				stmt.setObject(i + 1, constraints.get(i).value);
			}

			return executeQuery(stmt);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean rowExists(QueryStorage... storage) {
		ResultSet rs = selectRow(storage);

		try {
			rs.close();
			return rs.next();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int getPosition(QueryStorage... storage) {
		List<Constraint> constraints = new ArrayList<>();
		Field field = null;
		for(QueryStorage queryStorage : storage) {
			if(queryStorage instanceof Constraint) {
				constraints.add((Constraint) queryStorage);
			} else if(queryStorage instanceof Field) {
				field = (Field) queryStorage;
			} else {
				throw new RuntimeException("Invalid QueryStorage type. Must only contain Constraints and one Field");
			}
		}

		if(field == null) throw new RuntimeException("Invalid QueryStorage type. Must only contain Constraints and one Field");

		StringBuilder query = new StringBuilder("SELECT RANK() OVER (ORDER BY " + field.fieldName + " DESC) AS position FROM " + tableName);
		if(constraints.size() > 0) query.append(" WHERE ");
		constraint(constraints, query);

		try {
			PreparedStatement stmt = connection.prepareStatement(query.toString());
			for(int i = 0; i < constraints.size(); i++) {
				stmt.setObject(i + 1, constraints.get(i).value);
			}

			ResultSet rs = executeQuery(stmt);
			rs.next();
			return rs.getInt("position");
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public ResultSet getTop(int amount, Field field) {
		String query = "SELECT * FROM " + tableName + " ORDER BY " + field.fieldName + " DESC LIMIT " + amount;
		try {
			PreparedStatement stmt = connection.prepareStatement(query);

			return executeQuery(stmt);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public DatabaseMetaData getMetaData() {
		try {
			return connection.getMetaData();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void constraint(List<Constraint> constraints, StringBuilder query) {
		for(Constraint constraint : constraints) {
			String operator = constraint.operator == null ? "=" : constraint.operator;
			query.append(constraint.fieldName).append(" ").append(operator).append(" ?");
			if(constraints.indexOf(constraint) != constraints.size() - 1) query.append(" AND ");
		}
	}

	public void close() {
		try {
			connection.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
