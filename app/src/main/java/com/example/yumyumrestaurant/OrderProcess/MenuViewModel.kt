package com.example.yumyumrestaurant.OrderProcess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.AdvancedFilter
import com.example.yumyumrestaurant.data.Menu.MenuDataSource
import com.example.yumyumrestaurant.data.Menu.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(
    private val menuRepository: MenuRepository = MenuRepository(MenuDataSource())
) : ViewModel() {
    private val _menuUiState = MutableStateFlow(MenuUiState(selectedCategory = "All"))
    val menuUiState: StateFlow<MenuUiState> = _menuUiState

    private val _advancedFilter = MutableStateFlow(AdvancedFilter())
    val advancedFilter: StateFlow<AdvancedFilter> = _advancedFilter

    private val _isAdvancedMode = MutableStateFlow(false)
    val isAdvancedMode: StateFlow<Boolean> = _isAdvancedMode

    val subCategories: StateFlow<List<String>> =
        menuUiState.map { state ->
            state.menuItems.map { it.category }.distinct()
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            emptyList()
        )

    val filterMenuItem: StateFlow<List<MenuItemUiState>> =
        combine(menuUiState, advancedFilter) { state, filter ->
            val query = state.searchQuery.trim().lowercase()

            state.menuItems.filter { item ->
                val matchesQuery =
                    item.foodName.lowercase().contains(query) ||
                            item.category.lowercase().contains(query)

                val subCategoryMatch =
                    filter.selectedSubCategories.contains("All") ||
                            filter.selectedSubCategories.contains(item.category)

                val minPriceMatch = filter.minPrice == null || item.price >= filter.minPrice
                val maxPriceMatch = filter.maxPrice == null || item.price <= filter.maxPrice

                val chefMatch = when(filter.chefOnly) {
                    true -> item.chefRecommend
                    false -> !item.chefRecommend
                    null -> true
                }

                matchesQuery && subCategoryMatch && minPriceMatch && maxPriceMatch && chefMatch
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val filteredSubCategories: StateFlow<List<String>> =
        combine(filterMenuItem, advancedFilter) { filteredItems, filter ->
            if (filter.selectedSubCategories.contains("All") && filter.minPrice == null && filter.maxPrice == null) {
                filterMenuItem.value.map { it.category }.distinct()
            } else {
                filteredItems.map { it.category }.distinct()
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    /*val filterMenuItem: StateFlow<List<MenuItemUiState>> =
        menuUiState.map { state ->
            val query = state.searchQuery.trim().lowercase()

            state.menuItems.filter { item ->
                val matchesQuery = item.foodName.lowercase().contains(query)
                val matchesCategory = item.category.lowercase().contains(query)
                val matchesSubCategory = item.subCategory.lowercase().contains(query)

                val matchCategoryFilter = state.selectedCategory == "All" || item.subCategory == state.selectedCategory

                matchCategoryFilter && (matchesQuery || matchesCategory || matchesSubCategory)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            emptyList()
        )*/

    init {
        fetchMenu()
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            _menuUiState.value = _menuUiState.value.copy(isLoading = true)

            val menuItems = menuRepository.getMenuItems()

            _menuUiState.value = _menuUiState.value.copy(
                menuItems = menuItems,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _menuUiState.value = _menuUiState.value.copy(searchQuery = newQuery)
    }

    fun setSelectedItem(item: MenuItemUiState) {
        _menuUiState.value = _menuUiState.value.copy(selectedItem = item)
    }

    fun setAdvancedFilter(filter: AdvancedFilter) {
        _advancedFilter.value = filter
    }

    // Apply advanced filter only when user presses Apply
    fun applyAdvancedFilter(filter: AdvancedFilter) {
        val allSubCategories = subCategories.value

        val normalizeSubCategories = autoSubCategorySelection(
            selected = filter.selectedSubCategories,
            allSubCategories = allSubCategories
        )

        val normalizedFilter = filter.copy(selectedSubCategories = normalizeSubCategories)

        _advancedFilter.value = normalizedFilter

        val isDefaultFilter = filter.selectedSubCategories == listOf("All") &&
                filter.minPrice == null &&
                filter.maxPrice == null &&
                filter.chefOnly == null

        _isAdvancedMode.value = !isDefaultFilter
    }

    fun setNormalFilter(selectedSubCategories: List<String>) {
        val allSubs = subCategories.value

        val normalizeSubCategories = autoSubCategorySelection(
            selected = selectedSubCategories,
            allSubCategories = allSubs
        )

        _isAdvancedMode.value = false
        _advancedFilter.value = AdvancedFilter(selectedSubCategories = normalizeSubCategories)
    }

    private fun autoSubCategorySelection(
        selected: List<String>,
        allSubCategories: List<String>
    ): List<String> {
        val cleanedSelected = selected.filter { it != "All" }

        return if (cleanedSelected.size == allSubCategories.size) {
            listOf("All")
        } else {
            selected
        }
    }

    fun resetAdvancedFilter() {
        _advancedFilter.value = AdvancedFilter()
    }

    fun getChipsToShow(subCategories: List<String>): List<String> {
        return if (_isAdvancedMode.value) {
            // Advanced Mode: show only selected subcategories
            if (_advancedFilter.value.selectedSubCategories.contains("All"))
                listOf("All") + subCategories
            else
                _advancedFilter.value.selectedSubCategories
        } else {
            // Normal Mode: show all subcategories
            listOf("All") + subCategories
        }
    }

}