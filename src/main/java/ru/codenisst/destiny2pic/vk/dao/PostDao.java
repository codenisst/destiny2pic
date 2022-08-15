package ru.codenisst.destiny2pic.vk.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.codenisst.destiny2pic.vk.models.Content;
import ru.codenisst.destiny2pic.vk.models.Post;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component("dao")
public class PostDao {

    private Statement statement;

    @Autowired
    public PostDao(Connection connection) {
        try {
            System.out.println("База данных подключена!");
            statement = connection.createStatement();

            createTablePostIfExist(statement);
            createTableContentIfExist(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePosts(List<Post> posts) throws SQLException {
        for (Post post : posts) {

            String linkId = post.getOwnerId() + "_" + post.getId();

            /* TODO
                    Придумать, как избежать детекта символа ' в строке
                    "Новая работа от Flauzino_FLZ - Lubrae's Ruin."
                    вызывает SQLITE_ERROR [SQLITE_ERROR] SQL error or missing database
                    (near "s": syntax error).
                    Пофиксить экзепшены от ResultSet (может быть только один resultSet у statement)
             */
            String valuePostQuery = String.format("INSERT OR FAIL INTO post " +
                            "VALUES ('%s', %d, %d, '%s');",
                    linkId, post.getOwnerId(), post.getId(), post.getText());

            statement.executeUpdate(valuePostQuery);

            for (Content content : post.getContentList()) {

                String contentLinkId = linkId + "_" + content.getId();
                String valueContent = String.format("INSERT OR FAIL INTO content " +
                                "VALUES ('%s', '%s', %d, '%s', '%s');",
                        contentLinkId, linkId, content.getId(), content.getType(), content.getUrl());

                statement.executeUpdate(valueContent);
            }
        }
    }

    public List<Post> getAllSavedPostsWithPictures() throws SQLException {
        List<Post> result = new ArrayList<>();

        String queryPost = "SELECT post_link_id, post_owner_id, post_id, post_text " +
                        "FROM post;";

        ResultSet postsSet = statement.executeQuery(queryPost);

            while (postsSet.next()) {

                String linkId = postsSet.getString("post_link_id");
                String queryContent = "SELECT content_id, content_type, content_url " +
                        "FROM content " +
                        "WHERE post_link_id = '" + linkId + "' " +
                        "AND content_type = 'photo'";

                ResultSet contentSet = statement.executeQuery(queryContent);

                List<Content> content = new ArrayList<>();
                while (contentSet.next()) {
                    content.add(new Content(contentSet.getInt("content_id"),
                            contentSet.getString("content_type"),
                            contentSet.getString("content_url")));
                }

                result.add(new Post(postsSet.getInt("post_owner_id"),
                        postsSet.getInt("post_id"),
                        postsSet.getString("post_text"),
                        content));
            }

        return result;
    }

    public void removeAllFromGroup(int groupId) {
        try {
            String query1 = "PRAGMA foreign_keys = ON";
            statement.execute(query1);

            String query2 = "DELETE FROM post WHERE post_owner_id =" +
                    groupId;
            statement.execute(query2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllGroupsIdsFromDatabase() throws SQLException {
        String query = "SELECT post_owner_id " +
                "FROM post " +
                "GROUP BY post_owner_id";

        List<String> resultList = new ArrayList<>();
        ResultSet resultFromDB = statement.executeQuery(query);

        while (resultFromDB.next()) {
            resultList.add(resultFromDB.getString(1));
        }

        return resultList;
    }

    private void createTablePostIfExist(Statement statement) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS post " +
                "(" +
                "post_link_id           text NOT NULL PRIMARY KEY, " +
                "post_owner_id          integer NOT NULL, " +
                "post_id                integer NOT NULL, " +
                "post_text              text" +
                ");";

        statement.execute(query);
    }

    private void createTableContentIfExist(Statement statement) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS content " +
                "(" +
                "content_link_id        text NOT NULL PRIMARY KEY, " +
                "post_link_id           text NOT NULL, " +
                "content_id             integer NOT NULL, " +
                "content_type           text NOT NULL, " +
                "content_url            text NOT NULL, " +
                "FOREIGN KEY (post_link_id) " +
                "REFERENCES post(post_link_id) " +
                "ON DELETE CASCADE" +
                ");";

        statement.execute(query);
    }

    public void dropDb() {
        try {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("DELETE FROM post");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
