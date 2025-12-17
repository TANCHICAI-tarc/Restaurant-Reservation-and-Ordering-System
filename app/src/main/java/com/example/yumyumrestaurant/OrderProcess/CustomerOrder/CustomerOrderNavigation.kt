package com.example.yumyumrestaurant.OrderProcess.CustomerOrder

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.R

enum class CustomerOrderNavigation(@StringRes val title: Int) {
    MenuScreen(title = R.string.title_menu_selection),
    MenuDetailsScreen(title = R.string.title_menu_details),
    CartInfoScreen(title = R.string.title_cart_info),
    OrderSuccessScreen(title = R.string.title_order_success)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAppBar(
    currentScreen: CustomerOrderNavigation,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderNavigation(
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    navController: NavHostController
) {
    val bottomSheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val selectedItem = menuViewModel.menuUiState.collectAsState().value.selectedItem

    CustomerOrderNavigationContent(
        menuViewModel = menuViewModel,
        orderViewModel = orderViewModel,
        navController = navController,
        onShowSheet = {
            showSheet = true
        }
    )

    if(showSheet && selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState,
        ) {
            BottomSheetShow(
                item = selectedItem,
                onMakeAnother = {
                    showSheet = false
                    navController.navigate(CustomerOrderNavigation.MenuDetailsScreen.name)
                }
            )
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderNavigationContent(
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    navController: NavHostController,
    onShowSheet: () -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CustomerOrderNavigation.valueOf(
        backStackEntry?.destination?.route ?: CustomerOrderNavigation.MenuScreen.name
    )

    Scaffold(
        topBar = {
            if (currentScreen != CustomerOrderNavigation.OrderSuccessScreen) {
                OrderAppBar(
                    currentScreen = currentScreen,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CustomerOrderNavigation.MenuScreen.name,
            modifier = Modifier.padding(innerPadding)
        ) {

        }
    }
}

