package com.example.letslearn

data class File(
    val name:String,
    val url:String,
    val uploadedBy:String
) {
    constructor() : this("","","")
}