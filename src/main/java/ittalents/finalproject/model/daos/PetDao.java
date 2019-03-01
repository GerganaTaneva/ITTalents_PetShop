package ittalents.finalproject.model.daos;

import ittalents.finalproject.exceptions.PetNotFoundException;
import ittalents.finalproject.model.pojos.pets.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PetDao {

    @Autowired
    private JdbcTemplate db;

    public void addPet(Pet pet){

        String insertPet = "INSERT INTO pets(gender, breed, sub_breed, age, pet_desc, in_sale, price, quantity) VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
        KeyHolder key = new GeneratedKeyHolder();
        db.update((connection)-> {
            PreparedStatement ps = connection.prepareStatement(insertPet, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pet.getGender());
            ps.setString(2, pet.getBreed());
            ps.setString(3, pet.getSubBreed());
            ps.setInt(4, pet.getAge());
            ps.setString(5, pet.getPetDesc());
            ps.setBoolean(6, pet.isInSale());
            ps.setDouble(7, pet.getPrice());
            ps.setInt(8, pet.getQuantity());
            return ps;
            }, key);
        pet.setPosted(Timestamp.valueOf(LocalDateTime.now()));
        pet.setId(key.getKey().longValue());
    }

    public List<Pet> getAll(){
        String getAll = "SELECT id, gender, breed, sub_breed, age, posted, pet_desc, in_sale, price, quantity, posted FROM pets ORDER BY posted DESC;";
        List<Pet> pets = db.query(getAll, (resultSet, i) -> toPet(resultSet));
        return pets;
    }

    private Pet toPet(ResultSet resultSet) throws SQLException{
        Pet pet = new Pet(resultSet.getString("gender"),
                resultSet.getString("breed"),
                resultSet.getString("sub_breed"),
                resultSet.getInt("age"),
                resultSet.getTimestamp("posted"),
                resultSet.getString("pet_desc"),
                resultSet.getBoolean("in_sale"),
                resultSet.getDouble("price"),
                resultSet.getInt("quantity"));
        pet.setId(resultSet.getLong("id"));
        return pet;
    }

    public Pet getById(long id){
        String getById = "SELECT id, gender, breed, sub_breed, age, posted, pet_desc, in_sale, price, quantity FROM pets WHERE id = ?;";
        Pet pet;
        pet = db.query(getById, new Object[]{id}, (resultSet) -> {
            resultSet.next();
            return toPet(resultSet);
        });
        return pet;
    }

    //into dto to add photos
    public List<Pet> getFiltred(String breed, String subBreed, int fromAge, int toAge, String gender,
                                double fromPrice, double toPrice, String sortBy, String ascDesc) {
        String filter;
        List<Pet> pets = null;
        if(subBreed != null && fromAge > -1 && toAge > -1 && gender != null && fromPrice > 0 && toPrice > 0 &&
            sortBy != null && ascDesc != null) {
            if (fromAge > toAge) {
                int temp = fromAge;
                fromAge = toAge;
                toAge = temp;
            } else if (fromPrice > toPrice) {
                double temp = fromPrice;
                fromPrice = toPrice;
                toPrice = temp;
            }
            filter = "SELECT id, gender, breed, sub_breed, age, posted, pet_desc, in_sale, price, quantity FROM pets WHERE (age >= ? AND age <= ?)" +
                    " AND breed LIKE ? AND sub_breed LIKE ? AND gender LIKE ? && (price >= ? AND price <= ?) ORDER BY ? ?" +
                    " ORDER BY ? ?";
            pets = db.query(filter, new Object[]{fromAge, toAge, breed, subBreed, gender, fromPrice, toPrice}, (resultSet, i) -> toPet(resultSet));
        }
//       else if(){
//
//        }



       return pets;
    }

    public void delete(long id) throws PetNotFoundException {
        String deletePet = "DELETE FROM pets WHERE id = ?";
        db.query(deletePet, new Object[] {id}, (resultSet) -> {
            resultSet.next();
            toPet(resultSet);
        });
    }
}
