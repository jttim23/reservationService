package pl.jedro.reservationService;

import lombok.Data;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
public class Reservation {
  @Id
  @GeneratedValue
  private Long id;
  @OneToOne
  private Customer customer;
  private String additionalInfo;
  private Long tableId;
  private State state;
  public Reservation() {
  }


}
