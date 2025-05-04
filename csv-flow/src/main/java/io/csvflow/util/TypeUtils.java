package io.csvflow.util;

import java.math.BigDecimal;
import java.sql.Types;

public class TypeUtils {
    public static int mapSqlTypeToJavaType(String sqlType) {
        switch (sqlType) {
            case "varchar":
            case "text":
            case "char":
                return Types.VARCHAR;
            case "int":
            case "integer":
            case "smallint":
                return Types.INTEGER;
            case "decimal":
            case "numeric":
            case "float":
            case "double":
                return Types.DECIMAL;
            case "date":
            case "datetime":
            case "timestamp":
                return Types.TIMESTAMP;
            default:
                return Types.VARCHAR; // fallback
        }
    }

    public static boolean isValidValueForType(String value, int type) {
        try {
            switch (type) {
                case Types.INTEGER:
                    Integer.parseInt(value);
                    break;
                case Types.DECIMAL:
                    new BigDecimal(value.replace(",", "."));
                    break;
                case Types.TIMESTAMP:
                    java.sql.Timestamp.valueOf(value);
                    break;
                default:
                    break; // assume string is valid
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
