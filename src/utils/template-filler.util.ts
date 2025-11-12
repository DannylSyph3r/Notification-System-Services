// src/utils/template-filler.util.ts

/**
 * Replaces placeholders like {{key}} in a template string
 * with values provided in the `variables` object.
 */
export function fillTemplate(
  template: string,
  variables: Record<string, any>,
): string {
  if (!template) return '';

  let result = template;

  for (const [key, value] of Object.entries(variables)) {
    // Escape RegExp special characters inside keys (safety)
    const safeKey = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`{{\\s*${safeKey}\\s*}}`, 'g'); // allow spaces inside braces
    result = result.replace(regex, String(value ?? ''));
  }

  return result;
}
