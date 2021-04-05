import java.util.*;

/**
 * @author kebritam
 * Project SqlGenerator
 * Created on 05/04/2021
 */

public class SQL {

    private static final Builder builder = new Builder();
    private SQL() {
    }

    public static Builder builder() {
        return builder;
    }

    public static class Builder {
        private final Statement statement = new Statement();

        public Builder select(String... columns) {
            statement.queryType = Statement.QueryType.SELECT;
            statement.selects.addAll(Arrays.asList(columns));
            return this;
        }

        public Builder selectDistinct(String... columns) {
            statement.distinct = true;
            return select(columns);
        }

        public Builder from(String... tables) {
            statement.tables.addAll(Arrays.asList(tables));
            return this;
        }

        public Builder where(String... conditions) {
            statement.whereConditions.addAll(Arrays.asList(conditions));
            statement.andor = statement.whereConditions;
            return this;
        }

        public Builder and() {
            statement.andor.add(") \nAND (");
            return this;
        }

        public Builder or() {
            statement.andor.add(") \nOR (");
            return this;
        }

        public Builder orderBy(String... orderCondition) {
            statement.orderBys.addAll(Arrays.asList(orderCondition));
            return this;
        }


        public Builder update(String table) {
            statement.queryType = Statement.QueryType.UPDATE;
            statement.tables.add(table);
            return this;
        }

        public Builder set(String... settings) {
            statement.settings.addAll(Arrays.asList(settings));
            return this;
        }

        public Builder deleteFrom(String table) {
            statement.queryType = Statement.QueryType.DELETE;
            statement.tables.add(table);
            return this;
        }

        public Builder insertInto(String table, String... columns) {
            statement.queryType = Statement.QueryType.INSERT;
            statement.tables.add(table);
            statement.columns.addAll(Arrays.asList(columns));
            return this;
        }

        public Builder values(String... values) {
            statement.values.addAll(Arrays.asList(values));
            return this;
        }

        public String build() {
            return statement.query();
        }

    }

    private static class Statement {
        private enum QueryType {
            SELECT, INSERT, DELETE, UPDATE
        }

        private String query() {
            StringBuilder builder = new StringBuilder();
            String query;
            switch (queryType) {
                case SELECT:
                    query = selectQuery(builder);
                    break;
                case INSERT:
                    query = insertQuery(builder);
                    break;
                case DELETE:
                    query = deleteQuery(builder);
                    break;
                case UPDATE:
                    query = updateQuery(builder);
                    break;
                default:
                    return null;
            }
            cleanLists();
            return query;
        }

        private void cleanLists() {
            distinct = false;
            queryType = null;
            settings.clear();
            selects.clear();
            tables.clear();
            whereConditions.clear();
            orderBys.clear();
            columns.clear();
            values.clear();
            andor.clear();
        }

        private String selectQuery(StringBuilder builder) {
            sqlAppender(builder, "SELECT" + (distinct ? " DISTINCT" : ""), selects, "", "", ", ");
            sqlAppender(builder, "FROM", tables, "", "", ", ");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");
            sqlAppender(builder, "ORDER BY", orderBys, "", "", ", ");

            return builder.toString();
        }

        private String insertQuery(StringBuilder builder) {
            sqlAppender(builder, "INSERT INTO", tables, "", "", "");
            sqlAppender(builder, "", columns, "(", ")", ", ");
            sqlAppender(builder, "VALUES", values, "(", ")", ", ");

            return builder.toString();
        }

        private String deleteQuery(StringBuilder builder) {
            sqlAppender(builder, "DELETE FROM", tables, "", "", "");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");

            return builder.toString();
        }

        private String updateQuery(StringBuilder builder) {
            sqlAppender(builder, "UPDATE", tables, "", "", "");
            sqlAppender(builder, "SET", settings, "", "", ", ");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");

            return builder.toString();
        }

        public static void sqlAppender(StringBuilder builder, String keyword, List<String> parts, String open,
                                       String close, String separator) {
            final String AND = ") \nAND (";
            final String OR = ") \nOR (";

            if (!parts.isEmpty()) {
                builder.append("\n");
            }
            builder.append(keyword)
                    .append(" ")
                    .append(open);
            String last = "";
            for (int i = 0, count = parts.size(); i < count ; i++) {
                String part = parts.get(i);
                if (i > 0 && !part.equals(AND) && !part.equals(OR) && !last.equals(AND) && !last.equals(OR)) {
                    builder.append(separator);
                }
                builder.append(part);
                last = part;
            }
            builder.append(close);
        }

        private QueryType queryType;
        private boolean distinct;
        private final List<String> settings = new LinkedList<>();
        private final List<String> selects = new LinkedList<>();
        private final List<String> tables = new LinkedList<>();
        private final List<String> whereConditions = new LinkedList<>();
        private final List<String> orderBys = new LinkedList<>();
        private final List<String> columns = new LinkedList<>();
        private final List<String> values = new LinkedList<>();
        private List<String> andor = new LinkedList<>();

    }
}
