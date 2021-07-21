package pl.jedro.reservationService;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class ReservationService {
private ReservationRepository reservationRepository;
  private final RestTemplate restTemplate;

  public ReservationService(ReservationRepository reservationRepository, RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
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
    String url = "http://127.0.0.1:8089/v1/restaurants/tables/setState/"+reservation.getTableId();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentLength(0L);

    headers.setAccept(Collections.singletonList(MediaType.ALL));

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("state","PENDING");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    this.restTemplate.put(builder.toUriString(), entity);
    reservation.setState(State.PENDING);
    return reservation;
  }

  public List<Reservation> findAllReservations() {
 return reservationRepository.findAll();
  }

}
