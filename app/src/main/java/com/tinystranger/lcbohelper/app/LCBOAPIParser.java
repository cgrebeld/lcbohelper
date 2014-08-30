package com.tinystranger.lcbohelper.app;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 8/11/2014.
 */
public class LCBOAPIParser {
    private static String prodSearch = "{\n" +
            "  \"status\": 200,\n" +
            "  \"message\": null,\n" +
            "  \"pager\": {\n" +
            "    \"records_per_page\": 20,\n" +
            "    \"total_record_count\": 4,\n" +
            "    \"current_page_record_count\": 4,\n" +
            "    \"is_first_page\": true,\n" +
            "    \"is_final_page\": true,\n" +
            "    \"current_page\": 1,\n" +
            "    \"current_page_path\": \"/products?q=grouse&page=1\",\n" +
            "    \"next_page\": null,\n" +
            "    \"next_page_path\": null,\n" +
            "    \"previous_page\": null,\n" +
            "    \"previous_page_path\": null,\n" +
            "    \"total_pages\": 1,\n" +
            "    \"total_pages_path\": \"/products?q=grouse&page=1\"\n" +
            "  },\n" +
            "  \"result\": [{\n" +
            "    \"id\": 52050,\n" +
            "    \"is_dead\": false,\n" +
            "    \"name\": \"The Famous Grouse Scotch Whisky\",\n" +
            "    \"tags\": \"the famous grouse scotch whisky spirits whiskywhiskey united kingdom scotland highland distillers co bottle\",\n" +
            "    \"is_discontinued\": false,\n" +
            "    \"price_in_cents\": 2995,\n" +
            "    \"regular_price_in_cents\": 2995,\n" +
            "    \"limited_time_offer_savings_in_cents\": 0,\n" +
            "    \"limited_time_offer_ends_on\": null,\n" +
            "    \"bonus_reward_miles\": 0,\n" +
            "    \"bonus_reward_miles_ends_on\": null,\n" +
            "    \"stock_type\": \"LCBO\",\n" +
            "    \"primary_category\": \"Spirits\",\n" +
            "    \"secondary_category\": \"Whisky/Whiskey\",\n" +
            "    \"origin\": \"United Kingdom, Scotland\",\n" +
            "    \"package\": \"750 mL bottle\",\n" +
            "    \"package_unit_type\": \"bottle\",\n" +
            "    \"package_unit_volume_in_milliliters\": 750,\n" +
            "    \"total_package_units\": 1,\n" +
            "    \"volume_in_milliliters\": 750,\n" +
            "    \"alcohol_content\": 4000,\n" +
            "    \"price_per_liter_of_alcohol_in_cents\": 998,\n" +
            "    \"price_per_liter_in_cents\": 3993,\n" +
            "    \"inventory_count\": 5716,\n" +
            "    \"inventory_volume_in_milliliters\": 4287000,\n" +
            "    \"inventory_price_in_cents\": 17119420,\n" +
            "    \"sugar_content\": null,\n" +
            "    \"producer_name\": \"Highland Distillers Co.\",\n" +
            "    \"released_on\": null,\n" +
            "    \"has_value_added_promotion\": false,\n" +
            "    \"has_limited_time_offer\": false,\n" +
            "    \"has_bonus_reward_miles\": false,\n" +
            "    \"is_seasonal\": false,\n" +
            "    \"is_vqa\": false,\n" +
            "    \"is_kosher\": false,\n" +
            "    \"value_added_promotion_description\": null,\n" +
            "    \"description\": null,\n" +
            "    \"serving_suggestion\": \"With a dash of water to bring out the full flavour\",\n" +
            "    \"tasting_note\": \"Golden amber colour; fruit sweetness with soft smoke in the nose and flavour; medium-bodied with a round warm finish\",\n" +
            "    \"updated_at\": \"2014-08-28T14:26:12.174Z\",\n" +
            "    \"image_thumb_url\": \"http://www.lcbo.com/content/dam/lcbo/products/052050.jpg/jcr:content/renditions/cq5dam.thumbnail.319.319.png\",\n" +
            "    \"image_url\": \"http://www.lcbo.com/content/dam/lcbo/products/052050.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg\",\n" +
            "    \"varietal\": \"Scotland Blend\",\n" +
            "    \"style\": \"Medium & Smoky\",\n" +
            "    \"tertiary_category\": \"Scotch Whisky Blends\",\n" +
            "    \"sugar_in_grams_per_liter\": null,\n" +
            "    \"clearance_sale_savings_in_cents\": 0,\n" +
            "    \"has_clearance_sale\": false,\n" +
            "    \"product_no\": 52050\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 140822,\n" +
            "    \"is_dead\": false,\n" +
            "    \"name\": \"The Black Grouse Scotch Whisky\",\n" +
            "    \"tags\": \"the black grouse scotch whisky spirits whiskywhiskey united kingdom scotland highland distillers co bottle\",\n" +
            "    \"is_discontinued\": false,\n" +
            "    \"price_in_cents\": 3495,\n" +
            "    \"regular_price_in_cents\": 3495,\n" +
            "    \"limited_time_offer_savings_in_cents\": 0,\n" +
            "    \"limited_time_offer_ends_on\": null,\n" +
            "    \"bonus_reward_miles\": 0,\n" +
            "    \"bonus_reward_miles_ends_on\": null,\n" +
            "    \"stock_type\": \"LCBO\",\n" +
            "    \"primary_category\": \"Spirits\",\n" +
            "    \"secondary_category\": \"Whisky/Whiskey\",\n" +
            "    \"origin\": \"United Kingdom, Scotland\",\n" +
            "    \"package\": \"750 mL bottle\",\n" +
            "    \"package_unit_type\": \"bottle\",\n" +
            "    \"package_unit_volume_in_milliliters\": 750,\n" +
            "    \"total_package_units\": 1,\n" +
            "    \"volume_in_milliliters\": 750,\n" +
            "    \"alcohol_content\": 4000,\n" +
            "    \"price_per_liter_of_alcohol_in_cents\": 1165,\n" +
            "    \"price_per_liter_in_cents\": 4660,\n" +
            "    \"inventory_count\": 1862,\n" +
            "    \"inventory_volume_in_milliliters\": 1396500,\n" +
            "    \"inventory_price_in_cents\": 6507690,\n" +
            "    \"sugar_content\": null,\n" +
            "    \"producer_name\": \"Highland Distillers Co.\",\n" +
            "    \"released_on\": null,\n" +
            "    \"has_value_added_promotion\": false,\n" +
            "    \"has_limited_time_offer\": false,\n" +
            "    \"has_bonus_reward_miles\": false,\n" +
            "    \"is_seasonal\": false,\n" +
            "    \"is_vqa\": false,\n" +
            "    \"is_kosher\": false,\n" +
            "    \"value_added_promotion_description\": null,\n" +
            "    \"description\": null,\n" +
            "    \"serving_suggestion\": \"serve with smoked meat sandwich\",\n" +
            "    \"tasting_note\": \"clear amber colour; light intensity of hay, peat, honey, citrus, brine and iodine; creamy with gentle heat, nice smoky finish\",\n" +
            "    \"updated_at\": \"2014-08-28T14:26:10.270Z\",\n" +
            "    \"image_thumb_url\": \"http://www.lcbo.com/content/dam/lcbo/products/140822.jpg/jcr:content/renditions/cq5dam.thumbnail.319.319.png\",\n" +
            "    \"image_url\": \"http://www.lcbo.com/content/dam/lcbo/products/140822.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg\",\n" +
            "    \"varietal\": \"Scotland Blend\",\n" +
            "    \"style\": \"Medium & Smoky\",\n" +
            "    \"tertiary_category\": \"Scotch Whisky Blends\",\n" +
            "    \"sugar_in_grams_per_liter\": null,\n" +
            "    \"clearance_sale_savings_in_cents\": 0,\n" +
            "    \"has_clearance_sale\": false,\n" +
            "    \"product_no\": 140822\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 211334,\n" +
            "    \"is_dead\": false,\n" +
            "    \"name\": \"The Famous Grouse Scotch Whisky\",\n" +
            "    \"tags\": \"the famous grouse scotch whisky spirits whiskywhiskey united kingdom scotland highland distillers co bottle\",\n" +
            "    \"is_discontinued\": false,\n" +
            "    \"price_in_cents\": 4295,\n" +
            "    \"regular_price_in_cents\": 4295,\n" +
            "    \"limited_time_offer_savings_in_cents\": 0,\n" +
            "    \"limited_time_offer_ends_on\": null,\n" +
            "    \"bonus_reward_miles\": 0,\n" +
            "    \"bonus_reward_miles_ends_on\": null,\n" +
            "    \"stock_type\": \"LCBO\",\n" +
            "    \"primary_category\": \"Spirits\",\n" +
            "    \"secondary_category\": \"Whisky/Whiskey\",\n" +
            "    \"origin\": \"United Kingdom, Scotland\",\n" +
            "    \"package\": \"1140 mL bottle\",\n" +
            "    \"package_unit_type\": \"bottle\",\n" +
            "    \"package_unit_volume_in_milliliters\": 1140,\n" +
            "    \"total_package_units\": 1,\n" +
            "    \"volume_in_milliliters\": 1140,\n" +
            "    \"alcohol_content\": 4000,\n" +
            "    \"price_per_liter_of_alcohol_in_cents\": 941,\n" +
            "    \"price_per_liter_in_cents\": 3767,\n" +
            "    \"inventory_count\": 4942,\n" +
            "    \"inventory_volume_in_milliliters\": 5633880,\n" +
            "    \"inventory_price_in_cents\": 21225890,\n" +
            "    \"sugar_content\": null,\n" +
            "    \"producer_name\": \"Highland Distillers Co.\",\n" +
            "    \"released_on\": null,\n" +
            "    \"has_value_added_promotion\": false,\n" +
            "    \"has_limited_time_offer\": false,\n" +
            "    \"has_bonus_reward_miles\": false,\n" +
            "    \"is_seasonal\": false,\n" +
            "    \"is_vqa\": false,\n" +
            "    \"is_kosher\": false,\n" +
            "    \"value_added_promotion_description\": null,\n" +
            "    \"description\": null,\n" +
            "    \"serving_suggestion\": \"serve with shortbread cookies\",\n" +
            "    \"tasting_note\": \"Golden amber colour; fruit sweetness with soft smoke in the nose and flavour; medium-bodied with a round warm finish\",\n" +
            "    \"updated_at\": \"2014-08-28T14:26:12.310Z\",\n" +
            "    \"image_thumb_url\": \"http://www.lcbo.com/content/dam/lcbo/products/211334.jpg/jcr:content/renditions/cq5dam.thumbnail.319.319.png\",\n" +
            "    \"image_url\": \"http://www.lcbo.com/content/dam/lcbo/products/211334.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg\",\n" +
            "    \"varietal\": \"Scotland Blend\",\n" +
            "    \"style\": \"Medium & Smoky\",\n" +
            "    \"tertiary_category\": \"Scotch Whisky Blends\",\n" +
            "    \"sugar_in_grams_per_liter\": null,\n" +
            "    \"clearance_sale_savings_in_cents\": 0,\n" +
            "    \"has_clearance_sale\": false,\n" +
            "    \"product_no\": 211334\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 332924,\n" +
            "    \"is_dead\": false,\n" +
            "    \"name\": \"Black Grouse Alpha\",\n" +
            "    \"tags\": \"black grouse alpha spirits whiskywhiskey united kingdom scotland highland distillers co bottle\",\n" +
            "    \"is_discontinued\": true,\n" +
            "    \"price_in_cents\": 3500,\n" +
            "    \"regular_price_in_cents\": 3500,\n" +
            "    \"limited_time_offer_savings_in_cents\": 0,\n" +
            "    \"limited_time_offer_ends_on\": null,\n" +
            "    \"bonus_reward_miles\": 0,\n" +
            "    \"bonus_reward_miles_ends_on\": null,\n" +
            "    \"stock_type\": \"LCBO\",\n" +
            "    \"primary_category\": \"Spirits\",\n" +
            "    \"secondary_category\": \"Whisky/Whiskey\",\n" +
            "    \"origin\": \"United Kingdom, Scotland\",\n" +
            "    \"package\": \"700 mL bottle\",\n" +
            "    \"package_unit_type\": \"bottle\",\n" +
            "    \"package_unit_volume_in_milliliters\": 700,\n" +
            "    \"total_package_units\": 1,\n" +
            "    \"volume_in_milliliters\": 700,\n" +
            "    \"alcohol_content\": 4000,\n" +
            "    \"price_per_liter_of_alcohol_in_cents\": 1250,\n" +
            "    \"price_per_liter_in_cents\": 5000,\n" +
            "    \"inventory_count\": 66,\n" +
            "    \"inventory_volume_in_milliliters\": 46200,\n" +
            "    \"inventory_price_in_cents\": 231000,\n" +
            "    \"sugar_content\": null,\n" +
            "    \"producer_name\": \"Highland Distillers Co.\",\n" +
            "    \"released_on\": null,\n" +
            "    \"has_value_added_promotion\": false,\n" +
            "    \"has_limited_time_offer\": false,\n" +
            "    \"has_bonus_reward_miles\": false,\n" +
            "    \"is_seasonal\": false,\n" +
            "    \"is_vqa\": false,\n" +
            "    \"is_kosher\": false,\n" +
            "    \"value_added_promotion_description\": null,\n" +
            "    \"description\": null,\n" +
            "    \"serving_suggestion\": null,\n" +
            "    \"tasting_note\": null,\n" +
            "    \"updated_at\": \"2014-08-28T14:04:58.756Z\",\n" +
            "    \"image_thumb_url\": null,\n" +
            "    \"image_url\": null,\n" +
            "    \"varietal\": \"Scotland Blend\",\n" +
            "    \"style\": \"Medium & Smoky\",\n" +
            "    \"tertiary_category\": \"Scotch Whisky Blends\",\n" +
            "    \"sugar_in_grams_per_liter\": null,\n" +
            "    \"clearance_sale_savings_in_cents\": 0,\n" +
            "    \"has_clearance_sale\": false,\n" +
            "    \"product_no\": 332924\n" +
            "  }],\n" +
            "  \"suggestion\": null\n" +
            "}";
    public enum QueryType {
        kProducts,
        kStores
    }

    public List<LCBOEntity> entries;

    public List<LCBOEntity> parse(QueryType aType, InputStream in) throws IOException {
        //JsonReader reader = new JsonReader(new StringReader(prodSearch));
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<LCBOEntity> entityList = new ArrayList<LCBOEntity>();

        reader.beginObject();
        while (reader.hasNext()) {
            String topName = reader.nextName();
            if (topName.equals("result")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    LCBOEntity entity = new LCBOEntity();
                    while (reader.hasNext()) {
                        try {
                            String name = reader.nextName();
                            if (reader.peek() == JsonToken.NULL) {
                                reader.skipValue();
                            } else if (name.equals("product_no")) {
                                entity.itemNumber = String.valueOf(reader.nextInt());
                            } else if (name.equals("name")) {
                                entity.itemName = reader.nextString();
                            } else if (name.equals("volume_in_milliliters")) {
                                entity.productSize = String.valueOf(reader.nextInt()) + " mL";
                            } else if (name.equals("stock_type")) {
                                entity.stock_type = reader.nextString();
                            } else if (name.equals("primary_category")) {
                                entity.primary_category = reader.nextString();
                            } else if (name.equals("secondary_category")) {
                                entity.secondary_category = reader.nextString();
                            } else if (name.equals("tertiary_category")) {
                                entity.tertiary_category = reader.nextString();
                            } else if (name.equals("price_in_cents")) {
                                int totcents = reader.nextInt();
                                int dollars = (int) Math.floor(totcents / 100);
                                int cents = totcents % 100;
                                entity.price = String.format("$%d.%02d", dollars, cents);
                                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
                                try {
                                    entity.priceNumber = numberFormat.parse(entity.price);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if (name.equals("regular_price_in_cents")) {
                                int totcents = reader.nextInt();
                                int dollars = (int) Math.floor(totcents / 100);
                                int cents = totcents % 100;
                                entity.regularPrice = String.format("$%d.%02d", dollars, cents);
                                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
                                try {
                                    entity.regularPriceNumber = numberFormat.parse(entity.price);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if (name.equals("inventory_count")) {
                                entity.productQuantity = reader.nextInt();
                            } else if (name.equals("origin")) {
                                entity.producingCountry = reader.nextString();
                            } else if (name.equals("producer_name")) {
                                entity.producer = reader.nextString();
                            } else if (name.equals("bonus_reward_miles")) {
                                entity.airMiles = reader.nextInt() > 0;
                            } else if (name.equals("has_limited_time_offer")) {
                                entity.limitedTimeOffer = reader.nextBoolean();
                            } else if (name.equals("style")) {
                                entity.wineStyle = reader.nextString();
                            } else if (name.equals("varietal")) {
                                entity.wineVerietal = reader.nextString();
                            } else if (name.equals("tasting_note")) {
                                entity.itemDescription = reader.nextString();
                            } else if (name.equals("image_thumb_url")) {
                                entity.image_thumb_url = reader.nextString();
                            } else if (name.equals("image_url")) {
                                entity.image_url = reader.nextString();
                            } else if (name.equals("serving_suggestion")) {
                                entity.serving_suggestion = reader.nextString();
                            } else if (name.equals("sugar_content")) {
                                entity.sweetnessDescriptor = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                    entityList.add(entity);
                }
                reader.endArray();

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return entityList;
    }
}
