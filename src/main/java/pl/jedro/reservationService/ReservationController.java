package pl.jedro.reservationService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {
  private ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }
  @GetMapping("/v1/reservations/all")
  public List<Reservation> getAllReservations(){
    return reservationService.findAllReservations();
  }
  @GetMapping("/v1/reservations/{reservationId}")
  public Reservation getReservation(@PathVariable Long reservationId){
    return reservationService.findReservationById(reservationId);
  }
  @PostMapping("/v1/reservations/new")
  public Reservation postReservation(@RequestBody Reservation reservation){
    return reservationService.saveNewReservation(reservation);
  }
}
