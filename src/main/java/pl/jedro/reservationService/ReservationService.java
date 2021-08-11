package pl.jedro.reservationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.jedro.reservationService.model.DTOs.ReservationListDTO;
import pl.jedro.reservationService.model.DTOs.RestaurantDTO;
import pl.jedro.reservationService.model.Reservation;
import pl.jedro.reservationService.model.State;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
  private final ReservationRepository reservationRepository;
  private final RestTemplate restTemplate;
  @Value("${server.port}")
  private String serverPort;

  public ReservationService(pl.jedro.reservationService.ReservationRepository reservationRepository, RestTemplate restTemplate) {
    this.reservationRepository = reservationRepository;
    this.restTemplate = restTemplate;
  }

  public Reservation findReservationById(Long reservationId) {
    if (reservationId == null) {
      throw new IllegalArgumentException();
    }
    return reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
  }

  public Reservation saveNewReservation(Reservation reservation) {
    if (reservation == null) {
      throw new IllegalArgumentException();
    }
    if (reservation.getState() != null) {
      throw new IllegalArgumentException();
    }
    reservation.setState(State.PENDING);
    reservationRepository.save(reservation);
   sendConfirmationEmail(reservation);
    sendDeskToPendingRequest(reservation);
    return reservation;
  }
  public void sendConfirmationEmail(Reservation reservation) {
    String localUrl = getLocalUrl();
    String confirmationLink = localUrl + "/v1/reservations/confirm/" + reservation.getId();
    String url = "https://MAILINGSERVICE/v1/mails/sendConfirmation";
    HttpHeaders headers = new HttpHeaders();

    headers.setAccept(Collections.singletonList(MediaType.ALL));
    headers.setContentLength(0L);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("customerEmail", reservation.getCustomerEmail()).queryParam("confirmationLink", confirmationLink);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    this.restTemplate.put(builder.toUriString(), entity);
  }


  public List<Reservation> findAllReservations() {
    return reservationRepository.findAll();
  }

  public Reservation confirmReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
    if (reservation.getState().equals(State.CONFIRMED)) {
      return reservation;
    }
    reservation.setState(State.CONFIRMED);
    reservationRepository.save(reservation);
    sendActivationEmail(reservation);
    return reservation;
  }

  private String getLocalUrl() {
    InetAddress localHost;
    try {
      localHost = Inet4Address.getLocalHost();
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException();
    }
    return "https://" + localHost.getHostAddress() + ":" + serverPort;
  }

  public Reservation activateReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
    reservation.setState(State.ACTIVE);
    reservationRepository.save(reservation);
    return reservation;
  }


  public Reservation cancelReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(IllegalArgumentException::new);
    reservation.setState(State.CANCELED);
    reservationRepository.save(reservation);
    return reservation;
  }

  private void sendDeskToPendingRequest(Reservation reservation) {
    Long restaurantId = reservation.getRestaurantId();
    Long deskId = reservation.getDeskId();
    if (restaurantId == null || deskId == null) {
      throw new IllegalArgumentException();
    }
    String url = "https://RESTAURANTSERVICE/v1/restaurants/" + restaurantId + "/tables/setState/" + deskId;

    HttpHeaders headers = new HttpHeaders();

    headers.setAccept(Collections.singletonList(MediaType.ALL));
    headers.setContentLength(0L);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("state", State.PENDING);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    this.restTemplate.put(builder.toUriString(), entity);
  }

  private void sendActivationEmail(Reservation reservation) {
    String restaurantEmail = getRestaurantEmail(reservation.getRestaurantId());
    String localUrl = getLocalUrl();
    String acceptationLink = localUrl + "/v1/reservations/activate/" + reservation.getId();
    String cancelLink = localUrl + "/v1/reservations/cancel/" + reservation.getId();
    String url = "https://MAILINGSERVICE/v1/mails/sendActivation";
    HttpHeaders headers = new HttpHeaders();

    headers.setAccept(Collections.singletonList(MediaType.ALL));
    headers.setContentLength(0L);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("restaurantEmail", restaurantEmail).queryParam("acceptationLink", acceptationLink)
        .queryParam("cancelLink", cancelLink);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    this.restTemplate.put(builder.toUriString(), entity);

  }

  private String getRestaurantEmail(Long restaurantId) {
    String url = "https://RESTAURANTSERVICE//v1/restaurants/" + restaurantId;
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    Optional<RestaurantDTO> restaurantOptional = Optional.ofNullable(restTemplate.getForObject(builder.toUriString(), RestaurantDTO.class));
    if (restaurantOptional.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return restaurantOptional.get().getEmailAddress();
  }

  public ReservationListDTO findAllReservationsByRestaurantId(Long restaurantId) {
    ReservationListDTO reservationsDTO = new ReservationListDTO();
    List<Reservation> reservations = findAllReservations();
    if (reservations.size() > 0) {
      reservations = reservations.stream().filter(reservation -> (reservation.getRestaurantId().equals(restaurantId)&&reservation.getState().equals(State.ACTIVE))).collect(Collectors.toList());
      reservationsDTO.setReservations(reservations);
    }
    return reservationsDTO;
  }
}
