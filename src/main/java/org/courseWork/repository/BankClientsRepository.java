package org.courseWork.repository;

import org.courseWork.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class BankClientsRepository {
    private final JdbcTemplate jdbcTemplate;

    public BankClientsRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
public Client findById (UUID id) {
    String sql = "SELECT * FROM PUBLIC.USERS WHERE ID = ?";
    return jdbcTemplate.queryForObject(sql, new Object[]{id.toString()}, (rs, rownum) -> {
        Client client = new Client();
        client.setId(UUID.fromString(rs.getString("ID")));
        client.setUserName(rs.getString("USERNAME"));
        client.setFirstName(rs.getString("FIRST_NAME"));
        client.setLastName(rs.getString("LAST_NAME"));
        return client;
    });
}
    public List<Client> findAll() {
        String sql = "SELECT * FROM PUBLIC.USERS";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Client client = new Client();
            client.setId((UUID)rs.getObject("ID"));
            client.setUserName(rs.getString("USERNAME"));
            client.setFirstName(rs.getString("FIRST_NAME"));
            client.setLastName(rs.getString("LAST_NAME"));
            return client;
    });
    }


}