<template>
  <div class="card shadow mb-4">
    <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
      <h6 style="margin: 0">{{ title }}</h6>
      <i :class="[icon, 'fa-solid', 'fa-2x']"></i>
    </div>
    <div class="card-body">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({ title: String, icon: String })
</script>
