import React from 'react'

export default function SubmitButton({str}: {str: string}) {
  return (
    <button
          type="submit"
          className="w-full mb-1 text-primaryDark border-1 py-2 rounded-md hover:bg-PrimaryDark hover:text-white transition ease-in-out duration-200"
        >
          {str}
        </button>
  )
}
