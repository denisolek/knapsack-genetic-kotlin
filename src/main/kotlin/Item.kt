class Item(
    val value: Double,
    val weight: Double
) {
    companion object {
        fun getItems(): List<Item> = listOf(
            Item(24.0, 12.0),
            Item(13.0, 7.0),
            Item(23.0, 11.0),
            Item(15.0, 8.0),
            Item(14.0, 9.0),
            Item(3.0, 6.0),
            Item(2.0, 5.0),
            Item(7.0, 14.0),
            Item(32.0, 12.0),
            Item(14.0, 91.0),
            Item(51.0, 23.0),
            Item(23.0, 7.0),
            Item(4.0, 5.0),
            Item(65.0, 30.0),
            Item(3.0, 4.0)
        )
    }
}