package com.marketboss
package dto

/**
 * Сущность товара. Товаром является sku, определяемый по внутреннему id магазина/маркетлейса
 * @param good_id внутренний id товара
 * @param title наименование товара (у некоторых магазинов может быть в разных полях бренд и имя, такие надо конкатенировать)
 * @param price_from наименьшая цена товара (типа "скидки" не нужны, только нижняя)
 * @param url сслыка на товар, для перехода из телеграма
* */
case class Good(good_id: String, title: String, price_from: Int, url: String)
