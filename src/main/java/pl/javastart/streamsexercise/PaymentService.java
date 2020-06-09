package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    List<Payment> findPaymentsSortedByDateDesc() {
        return paymentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> Integer.valueOf(payment.getPaymentDate().getYear()).equals(dateTimeProvider.yearMonthNow().getYear()))
                .filter(payment -> payment.getPaymentDate().getMonth().equals(dateTimeProvider.yearMonthNow().getMonth()))
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> Integer.valueOf(payment.getPaymentDate().getYear()).equals(dateTimeProvider.yearMonthNow().getYear()))
                .filter(payment -> payment.getPaymentDate().getMonth().equals(yearMonth.getMonth()))
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenLastDays(int days) {
        ZonedDateTime thirtyDaysAgo = dateTimeProvider.zonedDateTimeNow().plusDays(-days);
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getPaymentDate().toInstant().isAfter(thirtyDaysAgo.toInstant()))
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    Set<String> findProductsSoldInCurrentMonth() {
        return findPaymentsForCurrentMonth().stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth).stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth).stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getRegularPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(sumTotalForGivenMonth(yearMonth));
    }

    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> amountOfPayment(payment).compareTo(BigDecimal.valueOf(value)) > 0)
                .collect(Collectors.toSet());
    }

    BigDecimal amountOfPayment(Payment payment){
        return payment.getPaymentItems().stream()
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
