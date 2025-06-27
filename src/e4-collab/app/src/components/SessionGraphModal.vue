<template>
  <div class="modal show d-block" tabindex="-1">
    <div class="modal-dialog modal-fullscreen p-5">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Session {{ sessionId }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <SessionGraph :session-id="sessionId" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import SessionGraph from '@/components/SessionGraph.vue';

defineProps({ sessionId: Number });
</script>

<style scoped>
.modal {
  background: rgba(0, 0, 0, 0.5);
}
</style>