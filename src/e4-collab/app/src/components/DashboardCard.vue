<template>
  <div class="col-xl-3 col-md-6 mb-4">
    <div class="card border-left shadow h-100 py-2">
      <div class="card-body">
        <div class="row align-items-center">
          <div class="col mr-2">
            <h5>{{ title }}</h5>
            <h5>{{ value }}</h5>
          </div>
          <div class="col-auto">
            <i :class="[icon, 'fa-regular', 'fa-2x']"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ title: String, value: [String, Number], icon: String })
</script>
