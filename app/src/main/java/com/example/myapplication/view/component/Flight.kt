package com.example.myapplication.view.component

import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private typealias Airport = Flight.Airport

data class Flight(
    val time: LocalDateTime,
    val departure: Airport,
    val destination: Airport,
    @ColorRes val color: Int,
) {
    data class Airport(val city: String, val code: String)
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateFlights(): List<Flight> = buildList {
    val currentMonth = YearMonth.now()

    currentMonth.atDay(17).also { date ->
        add(
            Flight(
                date.atTime(14, 0),
                Airport("Lagos", "LOS"),
                Airport("Abuja", "ABV"),
                R.color.blue,
            ),
        )
        add(
            Flight(
                date.atTime(21, 30),
                Airport("Enugu", "ENU"),
                Airport("Owerri", "QOW"),
                R.color.red,
            ),
        )
    }

    currentMonth.atDay(22).also { date ->
        add(
            Flight(
                date.atTime(13, 20),
                Airport("Ibadan", "IBA"),
                Airport("Benin", "BNI"),
                R.color.brown_700,
            ),
        )
        add(
            Flight(
                date.atTime(17, 40),
                Airport("Sokoto", "SKO"),
                Airport("Ilorin", "ILR"),
                R.color.blue_grey_700,
            ),
        )
    }

    currentMonth.atDay(3).also { date ->
        add(
            Flight(
                date.atTime(20, 0),
                Airport("Makurdi", "MDI"),
                Airport("Calabar", "CBQ"),
                R.color.teal_700,
            ),
        )
    }

    currentMonth.atDay(12).also { date ->
        add(
            Flight(
                date.atTime(18, 15),
                Airport("Kaduna", "KAD"),
                Airport("Jos", "JOS"),
                R.color.black,
            ),
        )
    }

    currentMonth.plusMonths(1).atDay(13).also { date ->
        add(
            Flight(
                date.atTime(7, 30),
                Airport("Kano", "KAN"),
                Airport("Akure", "AKR"),
                R.color.candy_pink,
            ),
        )
        add(
            Flight(
                date.atTime(10, 50),
                Airport("Minna", "MXJ"),
                Airport("Zaria", "ZAR"),
                R.color.red_orange,
            ),
        )
    }

    currentMonth.minusMonths(1).atDay(9).also { date ->
        add(
            Flight(
                date.atTime(20, 15),
                Airport("Asaba", "ABB"),
                Airport("Port Harcourt", "PHC"),
                R.color.yellow,
            ),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
val flightDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")