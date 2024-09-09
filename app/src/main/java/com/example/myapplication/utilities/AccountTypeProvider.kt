package com.example.myapplication.utilities

import com.example.myapplication.data.AccountType

object AccountTypeProvider {
    val accountTypes = listOf(
        AccountType("Mặc định", ""), //0
        AccountType("Tiền mặt", ""), //1
        AccountType("Thẻ ghi nợ", ""), //2
        AccountType("Thẻ tín dụng", "(Nợ phải trả)"), //3
        AccountType("Tài khoản ảo", ""), //4
        AccountType("Đầu tư", ""), //5
        AccountType("Nợ tôi / Tài khoản phải thu", ""), //6
        AccountType("Tôi nợ / Tài khoản phải trả", "(Nợ phải trả)") //7
    )

    val reminderTypes = listOf(
        AccountType("Hàng ngày", ""), //1
        AccountType("Hàng tuần", ""), //2
        AccountType("Hàng tháng", ""), //3
    )
}
