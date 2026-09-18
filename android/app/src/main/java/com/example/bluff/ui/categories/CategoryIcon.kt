package com.example.bluff.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

private val materialIconMap: Map<String, ImageVector> = mapOf(
    // Money
    "payments" to Icons.Rounded.Payments,
    "account_balance" to Icons.Rounded.AccountBalance,
    "savings" to Icons.Rounded.Savings,
    "credit_card" to Icons.Rounded.CreditCard,
    "money" to Icons.Rounded.Payments,
    "attach_money" to Icons.Rounded.AttachMoney,
    "monetization_on" to Icons.Rounded.MonetizationOn,
    "receipt" to Icons.Rounded.Receipt,
    "wallet" to Icons.Rounded.AccountBalanceWallet,
    "paid" to Icons.Rounded.Paid,
    "trending_up" to Icons.AutoMirrored.Rounded.TrendingUp,
    "show_chart" to Icons.AutoMirrored.Rounded.ShowChart,
    "currency_exchange" to Icons.Rounded.CurrencyExchange,
    "request_quote" to Icons.Rounded.RequestQuote,
    // Transport
    "directions_car" to Icons.Rounded.DirectionsCar,
    "directions_bus" to Icons.Rounded.DirectionsBus,
    "flight" to Icons.Rounded.Flight,
    "train" to Icons.Rounded.Train,
    "local_taxi" to Icons.Rounded.LocalTaxi,
    "bike_scooter" to Icons.Rounded.BikeScooter,
    "directions_boat" to Icons.Rounded.DirectionsBoat,
    "local_shipping" to Icons.Rounded.LocalShipping,
    "gas_station" to Icons.Rounded.LocalGasStation,
    "ev_station" to Icons.Rounded.EvStation,
    "parking" to Icons.Rounded.LocalParking,
    "traffic" to Icons.Rounded.Traffic,
    "route" to Icons.Rounded.Route,
    "map" to Icons.Rounded.Map,
    // Food
    "restaurant" to Icons.Rounded.Restaurant,
    "local_cafe" to Icons.Rounded.LocalCafe,
    "local_bar" to Icons.Rounded.LocalBar,
    "bakery_dining" to Icons.Rounded.BakeryDining,
    "lunch_dining" to Icons.Rounded.LunchDining,
    "dinner_dining" to Icons.Rounded.DinnerDining,
    "icecream" to Icons.Rounded.Icecream,
    "local_pizza" to Icons.Rounded.LocalPizza,
    "ramen_dining" to Icons.Rounded.RamenDining,
    "liquor" to Icons.Rounded.Liquor,
    "coffee" to Icons.Rounded.Coffee,
    "egg_alt" to Icons.Rounded.EggAlt,
    "kebab_dining" to Icons.Rounded.KebabDining,
    "brunch_dining" to Icons.Rounded.BrunchDining,
    "popcorn" to Icons.Rounded.Fastfood,
    "wb_sunny" to Icons.Rounded.WbSunny,
    "dark_mode" to Icons.Rounded.DarkMode,
    // Home
    "home" to Icons.Rounded.Home,
    "apartment" to Icons.Rounded.Apartment,
    "villa" to Icons.Rounded.Villa,
    "cottage" to Icons.Rounded.Cottage,
    "roofing" to Icons.Rounded.Roofing,
    "plumbing" to Icons.Rounded.Plumbing,
    "electrical_services" to Icons.Rounded.ElectricalServices,
    "hardware" to Icons.Rounded.Hardware,
    "cleaning_services" to Icons.Rounded.CleaningServices,
    "laundry" to Icons.Rounded.LocalLaundryService,
    "dry_cleaning" to Icons.Rounded.DryCleaning,
    "pest_control" to Icons.Rounded.PestControl,
    "yard" to Icons.Rounded.Yard,
    // Health
    "local_hospital" to Icons.Rounded.LocalHospital,
    "medical_services" to Icons.Rounded.MedicalServices,
    "medication" to Icons.Rounded.Medication,
    "vaccines" to Icons.Rounded.Vaccines,
    "health_and_safety" to Icons.Rounded.HealthAndSafety,
    "fitness_center" to Icons.Rounded.FitnessCenter,
    "spa" to Icons.Rounded.Spa,
    "psychology" to Icons.Rounded.Psychology,
    "visibility" to Icons.Rounded.Visibility,
    "bloodtype" to Icons.Rounded.Bloodtype,
    "monitor_heart" to Icons.Rounded.MonitorHeart,
    "healing" to Icons.Rounded.Healing,
    "self_improvement" to Icons.Rounded.SelfImprovement,
    // Leisure
    "sports_esports" to Icons.Rounded.SportsEsports,
    "movie" to Icons.Rounded.Movie,
    "music_note" to Icons.Rounded.MusicNote,
    "theaters" to Icons.Rounded.Theaters,
    "sports_soccer" to Icons.Rounded.SportsSoccer,
    "pool" to Icons.Rounded.Pool,
    "hiking" to Icons.Rounded.Hiking,
    "camping" to Icons.Rounded.OutdoorGrill,
    "skateboarding" to Icons.Rounded.Skateboarding,
    "surfing" to Icons.Rounded.Surfing,
    "sports_tennis" to Icons.Rounded.SportsTennis,
    "sports_basketball" to Icons.Rounded.SportsBasketball,
    "emoji_events" to Icons.Rounded.EmojiEvents,
    "celebration" to Icons.Rounded.Celebration,
    // Shopping
    "shopping_cart" to Icons.Rounded.ShoppingCart,
    "shopping_bag" to Icons.Rounded.ShoppingBag,
    "store" to Icons.Rounded.Store,
    "local_mall" to Icons.Rounded.LocalMall,
    "checkroom" to Icons.Rounded.Checkroom,
    "watch" to Icons.Rounded.Watch,
    "photo_camera" to Icons.Rounded.PhotoCamera,
    "devices" to Icons.Rounded.Devices,
    "phone_iphone" to Icons.Rounded.PhoneIphone,
    "laptop" to Icons.Rounded.Laptop,
    "headphones" to Icons.Rounded.Headphones,
    "toys" to Icons.Rounded.Toys,
    // Other
    "school" to Icons.Rounded.School,
    "flight_takeoff" to Icons.Rounded.FlightTakeoff,
    "beach_access" to Icons.Rounded.BeachAccess,
    "pets" to Icons.Rounded.Pets,
    "child_care" to Icons.Rounded.ChildCare,
    "elderly" to Icons.Rounded.Elderly,
    "groups" to Icons.Rounded.Groups,
    "volunteer_activism" to Icons.Rounded.VolunteerActivism,
    "menu_book" to Icons.AutoMirrored.Rounded.MenuBook,
    "lightbulb" to Icons.Rounded.Lightbulb,
    "bolt" to Icons.Rounded.Bolt,
    "language" to Icons.Rounded.Language,
    "inventory_2" to Icons.Rounded.Inventory2,
    "edit" to Icons.Rounded.Edit
)

@Composable
fun CategoryIcon(
    icon: String,
    iconType: String,
    color: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    backgroundSize: Dp = 48.dp,
    fontSize: TextUnit = 24.sp
) {
    val parsedColor = try {
        Color(color.toColorInt())
    } catch (e: Exception) {
        Color(0xFF888888)
    }

    Box(
        modifier = modifier
            .size(backgroundSize)
            .clip(CircleShape)
            .background(parsedColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (iconType == "material") {
            val vector = materialIconMap[icon]
            if (vector != null) {
                Icon(
                    imageVector = vector,
                    contentDescription = null,
                    tint = parsedColor,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                Text(icon.take(2), fontSize = fontSize)
            }
        } else {
            Text(icon, fontSize = fontSize)
        }
    }
}
