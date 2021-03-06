package ittalents.finalproject.model.pojos.pets;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderedPet {

    private long orderId;
    private long petId;
    private int quantity;
}
