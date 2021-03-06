package ittalents.finalproject.model.pojos.pets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
@Setter
public class PetInSale {

    private Long id;
    private Long petId;
    private Timestamp startDate;
    private Timestamp endDate;
    private Integer discountPrice;

    public PetInSale(Timestamp startDate, Timestamp endDate, int discountPrice) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPrice = discountPrice;
    }
}
