package pl.jedro.reservationService;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
private ReservationRepository reservationRepository;

  public ReservationService(ReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
  }

  public Reservation findReservationById(Long reservationId) {
    if (reservationId==null){
      throw new IllegalArgumentException();
    }
    return reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
  }

  public Reservation saveNewReservation(Reservation reservation) {
    if (reservation==null){
      throw new IllegalArgumentException();
    }
    reservationRepository.save(reservation);

    return reservation;
  }

  public List<Reservation> findAllReservations() {
 return reservationRepository.findAll();
  }

  public Reservation updateReservation(String customerName, String customerPhoneNumber, String additionalInfo, Long tableId, String status, Long reservationId) {
    if (reservationId==null){
      throw new IllegalArgumentException();
    }
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
    reservation.setCustomerName(customerName);
    reservation.setCustomerPhoneNumber(customerPhoneNumber);
    reservation.setAdditionalInfo(additionalInfo);
    reservation.setTableId(tableId);
    reservation.setStatus(status);
    reservationRepository.save(reservation);
    return reservation;
  }
}
