package com.example.letslearn

data class File(
    val name:String,
    val url:String,
    val uploadedBy:String,
    val mimeType:String
) {
    constructor() : this("","","","")
}