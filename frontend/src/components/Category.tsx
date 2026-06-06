import React from 'react';

export default function Category({ category }: {category: string}) {
  return (
    <span className="text-neutral-100 bg-gray-400 rounded-3xl p-2 m-0.5">{category}</span>
  )
}
