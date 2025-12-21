package com.example.yumyumrestaurant.OrderProcess.CustomerOrder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    allowOrdering: Boolean,
    onRequireReservation: () -> Unit,
    onNavigateToMenuDetails: (MenuItemUiState) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val menuState = menuViewModel.menuUiState.collectAsState().value
    val filterMenu = menuViewModel.filterMenuItem.collectAsState()
    val subCategories = menuViewModel.subCategories.collectAsState().value

    val keyboardController = LocalSoftwareKeyboardController.current

    var tempAdvancedFilter by remember { mutableStateOf(menuViewModel.advancedFilter.value) }
    var showReserveDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showAdvancedFilterDialog by remember { mutableStateOf(false) }
    val isAdvancedMode by menuViewModel.isAdvancedMode.collectAsState()
    val advancedFilterState = menuViewModel.advancedFilter.collectAsState().value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F3F3))
                .padding(
                    start = dimensionResource(R.dimen.dp_16),
                    end = dimensionResource(R.dimen.dp_16),
                    bottom = dimensionResource(R.dimen.dp_16),
                    top = dimensionResource(R.dimen.dp_16)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Advanced Filter button
                IconButton(
                    onClick = { showAdvancedFilterDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.FilterListOff, contentDescription = "Advanced Filter")
                }

                // Search bar
                OutlinedTextField(
                    value = menuState.searchQuery,
                    onValueChange = { menuViewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search food or drink...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (menuState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    menuViewModel.onSearchQueryChange("")
                                    keyboardController?.hide()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    }
                )
            }

            val chipsToShow = if (isAdvancedMode) {
                val visibleSubs = menuViewModel.filteredSubCategories.collectAsState().value
                if (visibleSubs.isEmpty()) listOf("All") else visibleSubs
            } else {
                listOf("All") + subCategories                 // normal mode
            }

            // Category buttons
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chipsToShow) { sub ->
                    val isSelected = advancedFilterState.selectedSubCategories.contains(sub)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isAdvancedMode) {
                                val current = advancedFilterState.selectedSubCategories.toMutableList()

                                if (sub == "All") {
                                    menuViewModel.setNormalFilter(listOf("All")) // reset
                                } else {
                                    current.remove("All")
                                    if (current.contains(sub)) current.remove(sub) else current.add(sub)
                                    if (current.isEmpty()) current.add("All")

                                    menuViewModel.setNormalFilter(current)
                                }
                            }

                        },
                        label = { Text(sub) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val groupedItems = filterMenu.value.groupBy { it.category }
            val cartItems = orderViewModel.orderUiState.collectAsState().value.selectedItems

            // Menu list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedItems.forEach { (subCategory, itemList) ->
                    // If user selected a category, only show that one
                    item(key = subCategory){
                        Text(
                            text = subCategory,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                    }

                    // Items in this subcategory
                    items(
                        items = itemList,
                        key = { it.menuID }
                    ) { item ->
                        val cartQuantity = cartItems.find { it.menuItem == item }?.quantity ?: 0

                        MenuItemCard(
                            item = item,
                            cartQuantity = cartQuantity,
                            onClick = {
                                if (allowOrdering) {
                                    onNavigateToMenuDetails(item)
                                } else {
                                    showReserveDialog = true
                                }
                            }
                        )
                    }

                }

            }

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (allowOrdering) {
                        onNavigateToCart()
                    } else {
                        showReserveDialog = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBasket,
                    contentDescription = "Cart",
                    modifier = Modifier.size(35.dp)
                )
            }

            val cartCount = orderViewModel.getTotalItemQuantity()
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(28.dp)
                        .background(Color(0xFF1565C0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        if (showAdvancedFilterDialog) {
            AdvancedFilterDialog(
                advancedFilter = tempAdvancedFilter,
                menuItems = menuState.menuItems,
                onUpdateFilter = { tempAdvancedFilter = it },
                onApplyFilter = {
                    menuViewModel.applyAdvancedFilter(tempAdvancedFilter)
                    showAdvancedFilterDialog = false
                },
                onClose = {
                    showAdvancedFilterDialog = false
                }
            )
        }

        if (showReserveDialog) {
            AlertDialog(
                onDismissRequest = { showReserveDialog = false },
                title = { Text("Reserve a Table First") },
                text = {
                    Text("Please reserve a table before placing any food orders.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showReserveDialog = false
                            onRequireReservation()
                        }
                    ) {
                        Text("Reserve Now")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReserveDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}

@Composable
fun MenuItemCard(
    item: MenuItemUiState,
    cartQuantity: Int,
    onClick: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.foodName,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Placeholder food name
                    Text(
                        text = item.foodName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Placeholder price
                    Text(
                        "RM ${"%.2f".format(item.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (item.chefRecommend) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star, // or any icon you prefer
                                contentDescription = "Chef's Recommendation",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Recommended",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        if (cartQuantity > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 8.dp)
                    .size(30.dp)
                    .background(Color(0xFF1565C0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cartQuantity.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterDialog(
    advancedFilter: AdvancedFilter,
    menuItems: List<MenuItemUiState>,
    onUpdateFilter: (AdvancedFilter) -> Unit,
    onApplyFilter: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = { /* Do nothing so dialog won't dismiss */ }) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 24.dp)
                    .background(Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Advanced Filter",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Subcategory selection
                    Text("Categories", fontWeight = FontWeight.SemiBold)
                    val allSubCategories = menuItems.map { it.category }.distinct()
                    val subCategoryOptions  = listOf("All") + allSubCategories
                    Column {
                        subCategoryOptions.forEach { sub ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable{
                                        val current = advancedFilter.selectedSubCategories.toMutableList()
                                        if (sub == "All") {
                                            onUpdateFilter(AdvancedFilter()) // Reset
                                        } else {
                                            current.remove("All")
                                            if (current.contains(sub)) current.remove(sub) else current.add(sub)
                                            val realSubCategories = subCategoryOptions.filter { it != "All" }
                                            val normalizedSelection = if (current.containsAll(realSubCategories)) {
                                                listOf("All")
                                            } else if (current.isEmpty()) {
                                                listOf("All")
                                            } else {
                                                current
                                            }
                                            onUpdateFilter(advancedFilter.copy(selectedSubCategories = normalizedSelection))
                                        }
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                val checked = advancedFilter.selectedSubCategories.contains(sub)
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null
                                )
                                Text(text = sub, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }

                    // PRICE FILTER
                    Text("Price Range (RM)", fontWeight = FontWeight.SemiBold)

                    val priceOptions = listOf("Default", "Below RM10", "RM10–RM20", "RM20+")
                    val selectedPriceIndex  = priceOptions.indexOfFirst { option ->
                        when(option) {
                            "Default" -> advancedFilter.minPrice == null && advancedFilter.maxPrice == null
                            "Below RM10" -> advancedFilter.maxPrice != null && advancedFilter.maxPrice <= 10
                            "RM10–RM20" -> advancedFilter.minPrice == 10.0 && advancedFilter.maxPrice == 20.0
                            "RM20+" -> advancedFilter.minPrice != null && advancedFilter.minPrice >= 20
                            else -> false
                        }
                    }.coerceAtLeast(0)
                    Column {
                        priceOptions.forEachIndexed { index, option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable{
                                        val newFilter = when(option) {
                                            "Default" -> advancedFilter.copy(minPrice = null, maxPrice = null)
                                            "Below RM10" -> advancedFilter.copy(minPrice = null, maxPrice = 10.0)
                                            "RM10–RM20" -> advancedFilter.copy(minPrice = 10.0, maxPrice = 20.0)
                                            "RM20+" -> advancedFilter.copy(minPrice = 20.0, maxPrice = null)
                                            else -> advancedFilter
                                        }
                                        onUpdateFilter(newFilter)
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                RadioButton(
                                    selected = index == selectedPriceIndex,
                                    onClick = null
                                )
                                Text(text = option, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }

                    Text("Chef Recommendation", fontWeight = FontWeight.SemiBold)

                    val chefOptions = listOf("Default" to null, "Recommended Dishes" to true, "Other Dishes" to false)

                    Column {
                        chefOptions.forEach { (label, value) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable{
                                        onUpdateFilter(advancedFilter.copy(chefOnly = value))
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                RadioButton(
                                    selected = advancedFilter.chefOnly == value,
                                    onClick = null
                                )
                                Text(text = label, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    //Action Buttons
                    val isResetEnabled = advancedFilter != AdvancedFilter()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                onUpdateFilter(AdvancedFilter())
                            },
                            enabled = isResetEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                onApplyFilter()
                                onClose()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-5).dp)
                    .size(48.dp)
                    .background(Color.Red, CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }
        }

    }

}