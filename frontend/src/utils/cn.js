import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Merge multiple class names safely resolving Tailwind CSS conflicts.
 * Combines clsx conditionals with tailwind-merge deduplication.
 *
 * @param {...any} inputs - Class values, objects, or arrays
 * @returns {string} - Combined class string
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}
