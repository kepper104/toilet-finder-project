package com.kepper104.toiletseverywhere.domain.model

data class ApiToilet (
    val id_: Int = 0,
    val author_id_: Int = 0,
    val coordinates_: String = "(0, 0)",
    val place_name_: String = "Public Toilet",
    val is_public_: Boolean = false,
    val disabled_access_: Boolean = false,
    val baby_access_: Boolean = false,
    val parking_nearby_: Boolean = true,
    val creation_date_: String = "2023-08-25",
    val opening_time_: String = "00:00:00",
    val closing_time_: String = "23:59:59",
    val cost_: Int = 0
)