package com.example.letslearn

data class User(
    val name:String,
    val phone:String,
    val standard:String,
    val school:String,
    val uid:String
){
    //Empty Constructor for firebase
    constructor() : this("","","","","")
}