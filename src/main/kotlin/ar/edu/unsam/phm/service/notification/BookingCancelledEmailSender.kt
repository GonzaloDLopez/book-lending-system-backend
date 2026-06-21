package ar.edu.unsam.phm.service.notification

import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class BookingCancelledEmailSender(
    private val mailSender:     MailSender,
    private val userRepository: UserRepository
) : BookingCancelledObserver {

    override fun bookingCancelled(booking: Booking) {
        val reader = userRepository.findById(booking.readerId).orElse(null) ?: return
        mailSender.sendMail(
            Mail(
                from    = "booklibre@unsam.edu.ar",
                to      = reader.email,
                subject = "Reserva cancelada",
                content = "Tu reserva del libro '${booking.bookTitle}' fue cancelada porque el libro fue dado de baja."
            )
        )
    }
}
