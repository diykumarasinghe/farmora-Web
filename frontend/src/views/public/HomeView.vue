<script setup>
import HomeHero from '../../components/home/HomeHero.vue'
import RoleCards from '../../components/home/RoleCards.vue'
import ShopByCategory from '../../components/home/ShopByCategory.vue'
import FeaturedProducts from '../../components/home/FeaturedProducts.vue'
import WhyFarmora from '../../components/home/WhyFarmora.vue'
import HowItWorks from '../../components/home/HowItWorks.vue'
import HomeCta from '../../components/home/HomeCta.vue'
</script>

<template>
  <div class="home-page">
    <HomeHero />
    <RoleCards />
    <ShopByCategory />
    <FeaturedProducts />
    <WhyFarmora />
    <HowItWorks />
    <HomeCta />
  </div>
</template>

<style scoped>
.home-page {
  background: #ffffff;
}
</style>
