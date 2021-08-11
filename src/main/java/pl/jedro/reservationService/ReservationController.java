package pl.jedro.reservationService;

import org.springframework.web.bind.annotation.*;
import pl.jedro.reservationService.model.DTOs.ReservationListDTO;
import pl.jedro.reservationService.model.Reservation;

import java.util.List;

@RestController
public class ReservationController {
  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping("/v1/reservations/all")
  public List<Reservation> getAllReservations() {
    return reservationService.findAllReservations();
  }

  @GetMapping("/v1/reservations/{reservationId}")
  public Reservation getReservation(@PathVariable Long reservationId) {
    return reservationService.findReservationById(reservationId);
  }

  @PostMapping("/v1/reservations/new")
  public Reservation postReservation(@RequestBody Reservation reservation) {
    return reservationService.saveNewReservation(reservation);
  }

  @PutMapping("v1/reservations/confirm/{reservationId}")
  public Reservation confirmReservation(@PathVariable Long reservationId) {
    return reservationService.confirmReservation(reservationId);
  }

  @PutMapping("v1/reservations/activate/{reservationId}")
  public Reservation activateReservation(@PathVariable Long reservationId) {
    return reservationService.activateReservation(reservationId);
  }

  @PutMapping("v1/reservations/cancel/{reservationId}")
  public Reservation cancelReservation(@PathVariable Long reservationId) {
    return reservationService.cancelReservation(reservationId);
  }

  @GetMapping("v1/reservations/all/{restaurantId}")
  public ReservationListDTO getAllReservations(@PathVariable Long restaurantId) {
    return reservationService.findAllReservationsByRestaurantId(restaurantId);
  }
}
