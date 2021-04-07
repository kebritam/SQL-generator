import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kebritam
 * Project SqlGenerator
 * Created on 05/04/2021
 */

public class SQL {

    private static final Builder builder = new Builder();
    private static final String AND = ") \nAND (";
    private static final String OR = ") \nOR (";

    private SQL() {
        throw new AssertionError("constructor should not be called");
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
            statement.lastThings = statement.whereConditions;
            return this;
        }

        public Builder groupBy(String... columns) {
            statement.groupBy.addAll(Arrays.asList(columns));
            return this;
        }

        public Builder having(String... conditions) {
            statement.having.addAll(Arrays.asList(conditions));
            statement.lastThings = statement.having;
            return this;
        }

        public Builder join(String... joins) {
            statement.join.addAll(Arrays.asList(joins));
            return this;
        }

        public Builder innerJoin(String... joins) {
            statement.innerJoin.addAll(Arrays.asList(joins));
            return this;
        }

        public Builder outerJoin(String... joins) {
            statement.outerJoin.addAll(Arrays.asList(joins));
            return this;
        }

        public Builder leftOuterJoin(String... joins) {
            statement.leftOuterJoin.addAll(Arrays.asList(joins));
            return this;
        }

        public Builder rightOuterJoin(String... joins) {
            statement.rightOuterJoin.addAll(Arrays.asList(joins));
            return this;
        }

        public Builder limit(int limit) {
            statement.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            statement.offset = offset;
            return this;
        }

        public Builder and() {
            statement.lastThings.add(AND);
            return this;
        }

        public Builder or() {
            statement.lastThings.add(OR);
            return this;
        }

        public Builder orderBy(String... orderCondition) {
            statement.orderBy.addAll(Arrays.asList(orderCondition));
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

        private QueryType queryType;
        private boolean distinct;
        private int limit = -1;
        private int offset = -1;
        private final List<String> settings = new LinkedList<>();
        private final List<String> selects = new LinkedList<>();
        private final List<String> tables = new LinkedList<>();
        private final List<String> whereConditions = new LinkedList<>();
        private final List<String> orderBy = new LinkedList<>();
        private final List<String> columns = new LinkedList<>();
        private final List<String> values = new LinkedList<>();
        private final List<String> groupBy = new LinkedList<>();
        private final List<String> having = new LinkedList<>();
        private List<String> lastThings = new LinkedList<>();
        private final List<String> join = new LinkedList<>();
        private final List<String> innerJoin = new LinkedList<>();
        private final List<String> outerJoin = new LinkedList<>();
        private final List<String> leftOuterJoin = new LinkedList<>();
        private final List<String> rightOuterJoin = new LinkedList<>();

        private static class StringWrapper {
            private final StringBuilder builder;
            private boolean isEmpty;

            public StringWrapper() {
                this.builder = new StringBuilder();
            }

            public StringWrapper append(String string) {
                if (isEmpty && string.length() > 0) {
                    isEmpty = false;
                }
                builder.append(string);
                return this;
            }

            public StringWrapper append(int number) {
                builder.append(number);
                return this;
            }

            @Override
            public String toString() {
                return builder.toString();
            }
        }

        private String query() {
            StringWrapper builder = new StringWrapper();
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
                    cleanStatement();
                    return null;
            }
            cleanStatement();
            return query;
        }

        private void cleanStatement() {
            distinct = false;
            queryType = null;
            offset = -1;
            limit = -1;
            settings.clear();
            selects.clear();
            tables.clear();
            whereConditions.clear();
            orderBy.clear();
            columns.clear();
            values.clear();
            lastThings.clear();
            join.clear();
            innerJoin.clear();
            outerJoin.clear();
            leftOuterJoin.clear();
            rightOuterJoin.clear();
        }

        private String selectQuery(StringWrapper builder) {
            sqlAppender(builder, "SELECT" + (distinct ? " DISTINCT" : ""), selects, "", "", ", ");
            sqlAppender(builder, "FROM", tables, "", "", ", ");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");
            join(builder);
            sqlAppender(builder, "GROUP BY", groupBy, "", "", ", ");
            sqlAppender(builder, "HAVING", having, "(", ")", " AND ");
            sqlAppender(builder, "ORDER BY", orderBy, "", "", ", ");
            limitOffset(builder, limit, offset);

            return builder.toString();
        }

        private void join(StringWrapper builder) {
            sqlAppender(builder, "JOIN", join, "", "", " JOIN ");
            sqlAppender(builder, "INNER JOIN", innerJoin, "", "", " INNER JOIN ");
            sqlAppender(builder, "OUTER JOIN", outerJoin, "", "", " OUTER JOIN ");
            sqlAppender(builder, "LEFT OUTER JOIN", leftOuterJoin, "", "", " LEFT OUTER JOIN ");
            sqlAppender(builder, "RIGHT OUTER JOIN", rightOuterJoin, "", "", " RIGHT OUTER JOIN ");
        }

        private String insertQuery(StringWrapper builder) {
            sqlAppender(builder, "INSERT INTO", tables, "", "", "");
            sqlAppender(builder, "", columns, "(", ")", ", ");
            sqlAppender(builder, "VALUES", values, "(", ")", ", ");

            return builder.toString();
        }

        private String deleteQuery(StringWrapper builder) {
            sqlAppender(builder, "DELETE FROM", tables, "", "", "");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");
            limitOffset(builder, limit, offset);

            return builder.toString();
        }

        private String updateQuery(StringWrapper builder) {
            sqlAppender(builder, "UPDATE", tables, "", "", "");
            join(builder);
            sqlAppender(builder, "SET", settings, "", "", ", ");
            sqlAppender(builder, "WHERE", whereConditions, "(", ")", " AND ");
            limitOffset(builder, limit, offset);

            return builder.toString();
        }

        private void limitOffset(StringWrapper builder, int limit, int offset) {
            if (limit >= 0) {
                builder.append(" LIMIT ")
                        .append(limit);
            }
            if (offset >= 0) {
                builder.append(" OFFSET ")
                        .append(offset);
            }
        }

        public void sqlAppender(StringWrapper builder, String keyword, List<String> parts, String open,
                                       String close, String separator) {
            if (!parts.isEmpty()) {

                if (!builder.isEmpty) {
                    builder.append("\n");
                }
                builder.append(keyword)
                        .append(" ")
                        .append(open);
                String last = "";
                for (int i = 0, count = parts.size(); i < count; i++) {
                    String part = parts.get(i);
                    if (i > 0 && !part.equals(AND) && !part.equals(OR) && !last.equals(AND) && !last.equals(OR)) {
                        builder.append(separator);
                    }
                    builder.append(part);
                    last = part;
                }
                builder.append(close);
            }
        }
    }
}
