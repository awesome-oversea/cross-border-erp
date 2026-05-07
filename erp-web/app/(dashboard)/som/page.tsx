'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function SomPage() {
  const router = useRouter();
  useEffect(() => { router.replace('/som/stores'); }, [router]);
  return null;
}
