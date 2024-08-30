package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.R
import com.example.myapplication.dao.AccountDao
import com.example.myapplication.dao.CategoryDao
import com.example.myapplication.dao.DailyReminderDao
import com.example.myapplication.dao.HistoryAccountDao
import com.example.myapplication.dao.IconsDao
import com.example.myapplication.dao.IncomeExpenseListDao
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.Icon
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [Category::class, Icon::class, IncomeExpenseList::class, Account::class, HistoryAccount::class, DailyReminder::class],
    version = 3,
    exportSchema = false
)
abstract class CategoryDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun iconDao(): IconsDao
    abstract fun incomeExpenseListDao(): IncomeExpenseListDao
    abstract fun accountDao(): AccountDao
    abstract fun historyAccountDao(): HistoryAccountDao
    abstract fun dailyReminderDao(): DailyReminderDao

    companion object {
        @Volatile
        private var INSTANCE: CategoryDatabase? = null
        private var initialized = false

        fun getDatabase(context: Context, scope: CoroutineScope): CategoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CategoryDatabase::class.java,
                    "category_database"
                ).addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    if (!initialized) {
                        scope.launch(Dispatchers.IO) {
                            insertSampleData(database.categoryDao(), database.iconDao())
                        }
                        initialized = true
                    }
                }
            }
        }

        private suspend fun insertSampleData(categoryDao: CategoryDao, iconDao: IconsDao) {
            val sampleIcons = listOf(
                Icon(iconResource = R.drawable.ic_game_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_ping_pong_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_chess_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_bowling_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_video_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_radio_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_handheld_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_gun_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_checkers_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_pacman_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_badminton_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_billiard_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_logic_game_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_robot_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_roller_skates_24, type = "Entertain"),
                Icon(iconResource = R.drawable.ic_pizza_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_bread_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_pet_food_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_hot_dog_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_candy_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_poultry_leg_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_confectionery_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_cake_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_cherry_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_fruit_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_burger_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_ice_cream_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_strawberry_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_milk_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_french_fries_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_rice_bowl_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_shrimp_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_sandwich_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_watermelon_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_ice_cream_2_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_pumpkin_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_pineapple_25, type = "Food"),
                Icon(iconResource = R.drawable.ic_fish_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_beer_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_alcohol_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_ladle_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_vegetable_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_burger_2_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_food_bar_24, type = "Food"),
                Icon(iconResource = R.drawable.ic_bag_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_hat_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_jewelry_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_underwear_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_high_heels_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_boots_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_dress_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_perfume_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_longsleeve_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_trousers_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_dress_2_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_diamond_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_shirt_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_watch_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_lipstick_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_tag_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_lip_gloss_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_glasses_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_glove_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_flip_flops_24, type = "Shopping"),
                Icon(iconResource = R.drawable.ic_coffee_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_coffee_2_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_bathtub_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_flashlight_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_television_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_furniture_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_air_conditioner_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_kitchenwares_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_umbrella_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_bed_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_camera_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_sofa_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_wardrobe_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_lamp_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_wall_painting_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_washing_machine_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_calculator_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_hairdryer_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_hanger_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_fan_24, type = "Left"),
                Icon(iconResource = R.drawable.ic_hair_brush_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_computer_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_guitar_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_candle_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_rose_bouquet_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_chair_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_love_message_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_zoosk_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_gift_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_relax_24, type = "Individual"),
                Icon(iconResource = R.drawable.ic_book_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_homework_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_school_backpack_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_school_2_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_ruler_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_telescope_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_earth_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_robot_2_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_microscope_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_draw_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_pencil_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_test_tube_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_board_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_graduation_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_checklist_24, type = "Education"),
                Icon(iconResource = R.drawable.ic_halloween_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_festival_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_carousel_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_party_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_christmas_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_firework_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_new_year_24, type = "Festival"),
                Icon(iconResource = R.drawable.ic_skiing_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_archery_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_golf_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_gym_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_badminton_2_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_football_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_swimming_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_cycling_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_ping_pong_2_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_tennis_24, type = "Sport"),
                Icon(iconResource = R.drawable.ic_phone_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_usb_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_hard_drive_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_paperclip_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_mouse_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_keyboard_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_printer_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_cloud_computing_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_document_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_cloud_storage_24, type = "Office"),
                Icon(iconResource = R.drawable.ic_car_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_train_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_ship_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_plane_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_moto_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_gas_station_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_bicycle_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_bus_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_taxi_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_helicopter_24, type = "Carriage"),
                Icon(iconResource = R.drawable.ic_inject_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_medicine_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_stethoscope_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_doctor_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_healthcare_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_hospital_bed_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_bandage_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_surgery_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_cast_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_tooth_24, type = "Health"),
                Icon(iconResource = R.drawable.ic_suitcase_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_island_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_surf_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_halal_food_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_hotel_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_martini_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_playground_24, type = "Tourism"),
                Icon(iconResource = R.drawable.ic_coin_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_bonds_24, type = "Finance"),
                Icon(
                    iconResource = R.drawable.ic_financial_dynamic_presentation_24,
                    type = "Finance"
                ),
                Icon(iconResource = R.drawable.ic_graph_report_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_money_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_dollar_bitcoin_exchange_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_topup_payment_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_duration_finance_24, type = "Finance"),
                Icon(iconResource = R.drawable.ic_binoculars_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_womens_bow_tie_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_letters_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_chip_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_internet_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_world_cup_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_mobile_texting_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_setting_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_repair_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_build_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_court_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_buying_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_tableware_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_phone_2_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_micro_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_book_3_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_group_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_shirt_3_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_cigarettes_pack_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_health_3_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_pet_3_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_paint_roller_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_present_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_charity_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_lotto_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_guacamole_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_children_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_paycheque_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_invest_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_time_money_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_reward_24, type = "Other"),
                Icon(iconResource = R.drawable.ic_money_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_money_dollar_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_euro_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_british_pound_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_credit_card_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_bitcoin_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_paypal_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_initiate_money_transfer_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_bank_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_saving_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_safe_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_wallet_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_payment_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_debt_50, type = "Account"),
                Icon(iconResource = R.drawable.ic_debt_2_50, type = "Account"),
            )

            val sampleCategories = listOf(
                Category(
                    name = "Hoa quả",
                    icon = 25,
                    type = "admin",
                    source = "Expense",
                    budget = "0",
                ),
                Category(
                    name = "Rau quả",
                    icon = 42,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Trẻ em",
                    icon = 198,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Đồ ăn nhẹ",
                    icon = 197,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Vé số",
                    icon = 196,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Quyên góp",
                    icon = 195,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Quà tặng",
                    icon = 194,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(name = "Nhà", icon = 70, type = "admin", source = "Expense", budget = "0"),
                Category(
                    name = "Nhà ở",
                    icon = 193,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Sửa chữa",
                    icon = 180,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Thú cưng",
                    icon = 192,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Sức khỏe",
                    icon = 191,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Du lịch",
                    icon = 140,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Thiết bị điện tử",
                    icon = 86,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Thuốc lá",
                    icon = 190,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Rượu",
                    icon = 116,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Xe hơi",
                    icon = 137,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Quần áo",
                    icon = 189,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Vận tải",
                    icon = 138,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Xã hội",
                    icon = 188,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Thể thao",
                    icon = 123,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Làm đẹp",
                    icon = 61,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Giáo dục",
                    icon = 187,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Giải trí",
                    icon = 186,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Điện thoại",
                    icon = 185,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Đồ ăn",
                    icon = 184,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),
                Category(
                    name = "Mua Sắm",
                    icon = 183,
                    type = "admin",
                    source = "Expense",
                    budget = "0"
                ),

                Category(
                    name = "Khác",
                    icon = 168,
                    type = "admin",
                    source = "Income",
                    budget = "0"
                ),
                Category(
                    name = "Lương",
                    icon = 199,
                    type = "admin",
                    source = "Income",
                    budget = "0"
                ),
                Category(
                    name = "Đầu tư",
                    icon = 200,
                    type = "admin",
                    source = "Income",
                    budget = "0"
                ),
                Category(
                    name = "Bán thời gian",
                    icon = 201,
                    type = "admin",
                    source = "Income",
                    budget = "0"
                ),
                Category(
                    name = "Giải thưởng",
                    icon = 202,
                    type = "admin",
                    source = "Income",
                    budget = "0"
                ),
            )

            withContext(Dispatchers.IO) {
                iconDao.insertAll(sampleIcons)
                categoryDao.insertAllCategory(sampleCategories)
            }
        }
    }
}