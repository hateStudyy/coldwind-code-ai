<template>
  <a-layout-footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <h3 class="footer-logo">WindCode</h3>
        <p class="footer-tagline">{{ t('home.subtitle') }}</p>
      </div>
      <div class="footer-links">
        <a href="https://github.com/hateStudyy" target="_blank" rel="noopener">GitHub</a>
        <RouterLink to="/">{{ t('nav.home') }}</RouterLink>
      </div>
    </div>
    <div class="footer-bottom">
      <p class="footer-copy">{{ t('footer.slogan') }}</p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import { useI18n } from '@/i18n'

const { t } = useI18n()
</script>

<style scoped>
.footer {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  padding: 0;
  margin-top: 80px;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 32px 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
}

.footer-brand {
  max-width: 360px;
}

.footer-logo {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #fa8c16, #ffd591);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-tagline {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  gap: 24px;
  padding-top: 4px;
}

.footer-links a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #fa8c16;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 20px 32px;
  text-align: center;
}

.footer-copy {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .footer-inner {
    flex-direction: column;
    gap: 24px;
    padding: 32px 20px 24px;
  }

  .footer-bottom {
    padding: 16px 20px;
  }
}
</style>
