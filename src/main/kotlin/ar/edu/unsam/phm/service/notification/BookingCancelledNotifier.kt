package ar.edu.unsam.phm.service.notification

import ar.edu.unsam.phm.domain.Booking
import org.springframework.stereotype.Service


interface BookingCancelledObserver {
    fun bookingCancelled(booking: Booking)
}

@Service
class BookingCancelledNotifier(
    private val observers: List<BookingCancelledObserver>
) {
    fun notify(booking: Booking) {
        observers.forEach { observer ->
            observer.bookingCancelled(booking)
        }
    }
}
